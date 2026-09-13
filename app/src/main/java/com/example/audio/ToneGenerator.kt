package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object ToneGenerator {
    private const val SAMPLE_RATE = 44100
    private var currentTrack: AudioTrack? = null

    suspend fun playStringTone(frequencyHz: Float, durationSeconds: Float = 2.2f) = withContext(Dispatchers.IO) {
        stopTone()
        val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
        val audioBuffer = ShortArray(numSamples)

        val omega1 = 2.0 * PI * frequencyHz / SAMPLE_RATE
        val omega2 = 2.0 * PI * (frequencyHz * 2.0) / SAMPLE_RATE
        val omega3 = 2.0 * PI * (frequencyHz * 3.0) / SAMPLE_RATE

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Pluck decay envelope
            val decay = exp(-1.8 * t)
            // Combine fundamental + rich musical harmonics
            val sample = (0.65 * sin(omega1 * i) + 0.25 * sin(omega2 * i) + 0.10 * sin(omega3 * i)) * decay
            audioBuffer[i] = (sample * Short.MAX_VALUE * 0.75).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(audioBuffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(audioBuffer, 0, audioBuffer.size)
        track.play()
        currentTrack = track
    }

    /**
     * Plays a brief, musical high chime to signal successful in-tune detection.
     */
    suspend fun playInTuneChime() = withContext(Dispatchers.IO) {
        try {
            val durationSeconds = 0.35f
            val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
            val audioBuffer = ShortArray(numSamples)

            val freq1 = 1046.50 // C6 chime
            val freq2 = 1318.51 // E6 harmony
            val omega1 = 2.0 * PI * freq1 / SAMPLE_RATE
            val omega2 = 2.0 * PI * freq2 / SAMPLE_RATE

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val decay = exp(-8.0 * t) // fast musical bell decay
                val sample = (0.6 * sin(omega1 * i) + 0.4 * sin(omega2 * i)) * decay
                audioBuffer[i] = (sample * Short.MAX_VALUE * 0.55).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val chimeTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(audioBuffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            chimeTrack.write(audioBuffer, 0, audioBuffer.size)
            chimeTrack.play()
        } catch (_: Exception) {
            // AudioTrack error safeguard
        }
    }

    fun stopTone() {
        try {
            currentTrack?.stop()
            currentTrack?.release()
        } catch (_: Exception) {
        } finally {
            currentTrack = null
        }
    }
}
