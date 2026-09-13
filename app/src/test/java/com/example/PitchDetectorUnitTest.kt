package com.example

import com.example.audio.PitchDetector
import com.example.audio.PitchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class PitchDetectorUnitTest {

    private val detector = PitchDetector()

    @Test
    fun testFrequencyDifferenceCalculation() {
        val targetHz = 440.0f
        val detectedHz = 442.5f

        val diff = detector.calculateFrequencyDifference(detectedHz, targetHz)
        assertEquals(2.5f, diff, 0.001f)

        val flatDetectedHz = 438.0f
        val flatDiff = detector.calculateFrequencyDifference(flatDetectedHz, targetHz)
        assertEquals(-2.0f, flatDiff, 0.001f)
    }

    @Test
    fun testCentsCalculation() {
        val targetHz = 440.0f

        // Exact match -> 0 cents
        val zeroCents = detector.calculateCents(targetHz, targetHz)
        assertEquals(0f, zeroCents, 0.01f)

        // Half step higher (A#4 ≈ 466.16 Hz) -> 100 cents
        val halfStepCents = detector.calculateCents(466.1638f, targetHz)
        assertEquals(100f, halfStepCents, 0.5f)

        // Half step lower (Ab4 ≈ 415.30 Hz) -> -100 cents
        val lowerHalfStepCents = detector.calculateCents(415.3047f, targetHz)
        assertEquals(-100f, lowerHalfStepCents, 0.5f)
    }

    @Test
    fun testNoteNameConversion() {
        assertEquals("A4", detector.frequencyToNoteName(440.0f))
        assertEquals("E2", detector.frequencyToNoteName(82.41f))
        assertEquals("C4", detector.frequencyToNoteName(261.63f))
        assertEquals("G3", detector.frequencyToNoteName(196.0f))
    }

    @Test
    fun testPitchResultFlags() {
        val target = 440.0f

        val inTuneResult = PitchResult(
            frequency = 440.2f,
            targetFrequency = target,
            frequencyDifference = 0.2f,
            cents = 0.8f,
            isInTune = true,
            isSilence = false
        )
        assertTrue(inTuneResult.isInTune)
        assertFalse(inTuneResult.isFlat)
        assertFalse(inTuneResult.isSharp)

        val flatResult = PitchResult(
            frequency = 436.0f,
            targetFrequency = target,
            frequencyDifference = -4.0f,
            cents = -15.8f,
            isInTune = false,
            isSilence = false
        )
        assertTrue(flatResult.isFlat)
        assertFalse(flatResult.isSharp)

        val sharpResult = PitchResult(
            frequency = 445.0f,
            targetFrequency = target,
            frequencyDifference = 5.0f,
            cents = 19.5f,
            isInTune = false,
            isSilence = false
        )
        assertTrue(sharpResult.isSharp)
        assertFalse(sharpResult.isFlat)
    }

    @Test
    fun testYinAlgorithmOnSyntheticSineWave() {
        val sampleRate = 44100
        val bufferSize = 4096
        val targetFreq = 440.0f // A4

        // Generate pure sine wave at 440 Hz
        val buffer = FloatArray(bufferSize) { i ->
            val t = i.toDouble() / sampleRate
            (0.8 * sin(2.0 * PI * targetFreq * t)).toFloat()
        }

        val (detectedFreq, clarity) = detector.detectPitchYin(
            buffer = buffer,
            sampleRate = sampleRate
        )

        // Frequency should be within 1 Hz of 440 Hz
        assertTrue("Detected $detectedFreq should be close to 440Hz", abs(detectedFreq - targetFreq) < 1.0f)
        assertTrue("Clarity $clarity should be high for pure sine", clarity > 0.85f)
    }

    @Test
    fun testUpdateTargetFrequencyForMultipleInstruments() {
        // 1. Guitar Low E string
        val guitarLowEHz = 82.41f
        detector.updateTargetFrequency(guitarLowEHz)
        assertEquals(guitarLowEHz, detector.pitchState.value.targetFrequency, 0.001f)

        // 2. Veena Sarani (Pa)
        val veenaPaHz = 196.00f
        detector.updateTargetFrequency(veenaPaHz)
        assertEquals(veenaPaHz, detector.pitchState.value.targetFrequency, 0.001f)

        // 3. Sitar Baaj (Ma)
        val sitarMaHz = 174.61f
        detector.updateTargetFrequency(sitarMaHz)
        assertEquals(sitarMaHz, detector.pitchState.value.targetFrequency, 0.001f)

        // 4. Violin 4G
        val violinGHz = 196.00f
        detector.updateTargetFrequency(violinGHz)
        assertEquals(violinGHz, detector.pitchState.value.targetFrequency, 0.001f)
    }
}
