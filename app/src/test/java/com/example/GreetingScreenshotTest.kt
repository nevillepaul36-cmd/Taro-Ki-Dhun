package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.audio.PitchResult
import com.example.data.model.InstrumentCatalog
import com.example.ui.components.GuitarTunaMeter
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun tuner_meter_screenshot() {
    val sampleString = InstrumentCatalog.instruments.first().strings.first()
    composeTestRule.setContent {
      MyApplicationTheme {
        GuitarTunaMeter(
          targetString = sampleString,
          pitchResult = PitchResult(frequency = 82.4f, note = "E2", cents = 0f, isInTune = true, isSilence = false),
          isListening = true
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tuner_meter.png")
  }
}
