package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.sqrt

/**
 * Result of real-time pitch detection analysis.
 *
 * @property frequency Detected fundamental frequency in Hertz.
 * @property targetFrequency Target frequency for the selected instrument string in Hertz.
 * @property frequencyDifference Real-time frequency difference (frequency - targetFrequency) in Hertz.
 *                             Positive value indicates sharp, negative indicates flat.
 * @property note Nearest musical note name (e.g., "E2", "A4").
 * @property cents Cent difference between detected pitch and target pitch (-50 to +50).
 * @property isInTune True if the detected frequency is within standard tuning tolerance (±3.5 cents).
 * @property amplitude Root-mean-square (RMS) volume of the audio signal (0.0 to 1.0).
 * @property isSilence True if audio energy is below the silence threshold.
 * @property clarity Pitch confidence / periodicity score (0.0 to 1.0).
 */
data class PitchResult(
    val frequency: Float = 0f,
    val targetFrequency: Float = 0f,
    val frequencyDifference: Float = 0f,
    val note: String = "-",
    val cents: Float = 0f,
    val isInTune: Boolean = false,
    val amplitude: Float = 0f,
    val isSilence: Boolean = true,
    val clarity: Float = 0f
) {
    /** True if string frequency is lower than target frequency. */
    val isFlat: Boolean get() = !isSilence && frequency > 0f && targetFrequency > 0f && cents < -3.5f

    /** True if string frequency is higher than target frequency. */
    val isSharp: Boolean get() = !isSilence && frequency > 0f && targetFrequency > 0f && cents > 3.5f

    /** Formatted frequency difference with sign, e.g. "+1.4 Hz" or "-2.1 Hz". */
    val formattedDiffHz: String
        get() {
            if (isSilence || frequency <= 0f || targetFrequency <= 0f) return "0.0 Hz"
            val sign = if (frequencyDifference > 0) "+" else ""
            return String.format(java.util.Locale.US, "%s%.1f Hz", sign, frequencyDifference)
        }
}

/**
 * Production-grade Pitch Detection Library for musical instruments.
 *
 * Uses standard Android [AudioRecord] APIs to capture microphone input and analyzes
 * monophonic audio signals using the YIN pitch tracking algorithm with parabolic sub-sample
 * interpolation, computing real-time frequency differences relative to the selected string.
 */
class PitchDetector(
    private val sampleRate: Int = SAMPLE_RATE,
    private val bufferSize: Int = BUFFER_SIZE
) {
    companion object {
        private const val TAG = "PitchDetector"
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 4096
        const val MIN_FREQ = 30.0f // Supports low bass, tanpura kharaj, and deep strings
        const val MAX_FREQ = 1400.0f // Supports high sitar frets, violin, mandolin
        const val SILENCE_THRESHOLD = 0.012f // RMS energy threshold
        const val YIN_THRESHOLD = 0.15f // Primary dip threshold for YIN algorithm
        const val IN_TUNE_TOLERANCE_CENTS = 3.5f // Musical tolerance for "in-tune" lock
    }

    private val _pitchState = MutableStateFlow(PitchResult())
    val pitchState: StateFlow<PitchResult> = _pitchState.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    @Volatile
    private var isListening = false

    // Exponential moving average for frequency smoothing
    private var smoothedFrequency = 0f

    /**
     * Starts analyzing microphone input in real-time via standard Android AudioRecord.
     *
     * @param coroutineScope Scope in which audio capture and analysis coroutine runs.
     * @param targetFrequencyProvider Callback returning the target frequency in Hz for the selected string.
     */
    @SuppressLint("MissingPermission")
    fun startListening(
        coroutineScope: CoroutineScope,
        targetFrequencyProvider: () -> Float?
    ) {
        if (isListening) return

        try {
            val minBufSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val actualBufSize = maxOf(minBufSize, bufferSize * 2)

            // Attempt initialization with MIC, fallback to DEFAULT if needed
            audioRecord = try {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    actualBufSize
                )
            } catch (e: Exception) {
                Log.w(TAG, "Falling back to AudioSource.DEFAULT: ${e.message}")
                AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    actualBufSize
                )
            }

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord could not be initialized")
                return
            }

            audioRecord?.startRecording()
            isListening = true
            smoothedFrequency = 0f

            recordJob = coroutineScope.launch(Dispatchers.Default) {
                val shortBuffer = ShortArray(bufferSize)
                val floatBuffer = FloatArray(bufferSize)

                while (isActive && isListening) {
                    val readCount = audioRecord?.read(shortBuffer, 0, bufferSize) ?: -1
                    if (readCount < bufferSize) {
                        delay(15)
                        continue
                    }

                    // 1. Convert PCM-16 short to normalized float [-1.0, 1.0] and compute RMS
                    var sumSquare = 0.0f
                    for (i in 0 until bufferSize) {
                        val sample = shortBuffer[i] / 32768.0f
                        floatBuffer[i] = sample
                        sumSquare += sample * sample
                    }
                    val rms = sqrt(sumSquare / bufferSize)

                    // 2. Silence / Noise Gating
                    if (rms < SILENCE_THRESHOLD) {
                        smoothedFrequency = 0f
                        _pitchState.value = _pitchState.value.copy(
                            amplitude = rms,
                            isSilence = true
                        )
                        delay(20)
                        continue
                    }

                    // 3. YIN Pitch Detection
                    val (detectedHz, clarity) = detectPitchYin(
                        buffer = floatBuffer,
                        sampleRate = sampleRate,
                        minFreq = MIN_FREQ,
                        maxFreq = MAX_FREQ,
                        yinThreshold = YIN_THRESHOLD
                    )

                    if (detectedHz in MIN_FREQ..MAX_FREQ && clarity > 0.40f) {
                        // Temporal smoothing: if within 8% of previous reading, smooth
                        smoothedFrequency = if (smoothedFrequency > 0f && abs(detectedHz - smoothedFrequency) / smoothedFrequency < 0.08f) {
                            0.7f * smoothedFrequency + 0.3f * detectedHz
                        } else {
                            detectedHz
                        }

                        val currentTarget = targetFrequencyProvider() ?: smoothedFrequency
                        val freqDiff = smoothedFrequency - currentTarget
                        val centsOffset = calculateCents(smoothedFrequency, currentTarget)
                        val inTune = abs(centsOffset) <= IN_TUNE_TOLERANCE_CENTS
                        val noteName = frequencyToNoteName(smoothedFrequency)

                        _pitchState.value = PitchResult(
                            frequency = smoothedFrequency,
                            targetFrequency = currentTarget,
                            frequencyDifference = freqDiff,
                            note = noteName,
                            cents = centsOffset.coerceIn(-50f, 50f),
                            isInTune = inTune,
                            amplitude = rms,
                            isSilence = false,
                            clarity = clarity
                        )
                    } else {
                        _pitchState.value = _pitchState.value.copy(
                            amplitude = rms,
                            isSilence = false,
                            clarity = clarity
                        )
                    }

                    delay(20)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting pitch detection: ${e.message}", e)
            stopListening()
        }
    }

    /**
     * Safely stops audio recording and releases hardware resources.
     */
    fun stopListening() {
        isListening = false
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing AudioRecord: ${e.message}")
        } finally {
            audioRecord = null
            smoothedFrequency = 0f
        }
        val previousTarget = _pitchState.value.targetFrequency
        _pitchState.value = PitchResult(targetFrequency = previousTarget)
    }

    /**
     * Explicitly updates the target frequency state for pitch detection logic.
     * Instantly recomputes frequency difference, cents deviation, and in-tune status.
     */
    fun updateTargetFrequency(targetHz: Float) {
        val current = _pitchState.value
        val diff = if (current.frequency > 0f && targetHz > 0f) current.frequency - targetHz else 0f
        val cents = if (current.frequency > 0f && targetHz > 0f) calculateCents(current.frequency, targetHz) else 0f
        val inTune = current.frequency > 0f && abs(cents) <= IN_TUNE_TOLERANCE_CENTS
        _pitchState.value = current.copy(
            targetFrequency = targetHz,
            frequencyDifference = diff,
            cents = cents.coerceIn(-50f, 50f),
            isInTune = inTune
        )
    }

    /**
     * Pure function to calculate cents deviation between detected and target frequencies.
     * Cents = 1200 * log2(detected / target)
     */
    fun calculateCents(detectedHz: Float, targetHz: Float): Float {
        if (detectedHz <= 0f || targetHz <= 0f) return 0f
        return (1200.0 * log2((detectedHz / targetHz).toDouble())).toFloat()
    }

    /**
     * Pure function to calculate frequency difference in Hertz.
     * Positive means sharp, negative means flat.
     */
    fun calculateFrequencyDifference(detectedHz: Float, targetHz: Float): Float {
        if (detectedHz <= 0f || targetHz <= 0f) return 0f
        return detectedHz - targetHz
    }

    /**
     * Converts a frequency in Hertz to standard scientific pitch notation (e.g. A4 = 440 Hz).
     */
    fun frequencyToNoteName(freq: Float): String {
        if (freq <= 0f) return "-"
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteNum = 12.0 * log2((freq / 440.0).toDouble()) + 69.0
        val roundedNote = Math.round(noteNum).toInt()
        val noteIndex = (roundedNote % 12 + 12) % 12
        val octave = (roundedNote / 12) - 1
        return "${noteNames[noteIndex]}$octave"
    }

    /**
     * YIN pitch estimation algorithm implementation with parabolic peak refinement.
     *
     * Returns Pair(detectedPitchHz, clarityScore).
     */
    fun detectPitchYin(
        buffer: FloatArray,
        sampleRate: Int,
        minFreq: Float = MIN_FREQ,
        maxFreq: Float = MAX_FREQ,
        yinThreshold: Float = YIN_THRESHOLD
    ): Pair<Float, Float> {
        val halfBufferSize = buffer.size / 2
        val minPeriod = (sampleRate / maxFreq).toInt().coerceAtLeast(2)
        val maxPeriod = (sampleRate / minFreq).toInt().coerceAtMost(halfBufferSize - 1)

        val yinBuffer = FloatArray(halfBufferSize)

        // Step 1: Squared Difference Function
        for (tau in 0 until halfBufferSize) {
            var sum = 0.0f
            for (i in 0 until halfBufferSize) {
                val delta = buffer[i] - buffer[i + tau]
                sum += delta * delta
            }
            yinBuffer[tau] = sum
        }

        // Step 2: Cumulative Mean Normalized Difference Function
        yinBuffer[0] = 1.0f
        var runningSum = 0.0f
        for (tau in 1 until halfBufferSize) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = if (runningSum > 0f) {
                (yinBuffer[tau] * tau) / runningSum
            } else {
                1.0f
            }
        }

        // Step 3: Absolute Thresholding & First Local Minimum Search
        var tauEstimate = -1
        for (tau in minPeriod..maxPeriod) {
            if (yinBuffer[tau] < yinThreshold) {
                // Find local minimum around this dip
                var bestTau = tau
                while (bestTau + 1 <= maxPeriod && yinBuffer[bestTau + 1] < yinBuffer[bestTau]) {
                    bestTau++
                }
                tauEstimate = bestTau
                break
            }
        }

        // Fallback: If no point dropped below threshold, find global minimum in range
        if (tauEstimate == -1) {
            var minVal = Float.MAX_VALUE
            for (tau in minPeriod..maxPeriod) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau]
                    tauEstimate = tau
                }
            }
            // If even global minimum is too noisy (> 0.50), reject
            if (minVal > 0.50f) {
                return Pair(0f, 0f)
            }
        }

        if (tauEstimate < 1 || tauEstimate >= halfBufferSize - 1) {
            return Pair(0f, 0f)
        }

        // Step 4: Parabolic Interpolation for Sub-sample Period Precision
        val s0 = yinBuffer[tauEstimate - 1]
        val s1 = yinBuffer[tauEstimate]
        val s2 = yinBuffer[tauEstimate + 1]
        val denominator = 2.0f * (2.0f * s1 - s0 - s2)
        val shift = if (denominator != 0.0f) (s2 - s0) / denominator else 0.0f
        val refinedPeriod = tauEstimate.toFloat() + shift.coerceIn(-0.5f, 0.5f)

        val detectedFrequency = if (refinedPeriod > 0f) sampleRate / refinedPeriod else 0f
        val clarity = (1.0f - yinBuffer[tauEstimate]).coerceIn(0f, 1f)

        return Pair(detectedFrequency, clarity)
    }
}
