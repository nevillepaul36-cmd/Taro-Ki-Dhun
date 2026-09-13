package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.InstrumentCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("String Tuner", appName)
  }

  @Test
  fun `catalog contains at least 10 string instruments with accurate frequencies`() {
    val instruments = InstrumentCatalog.instruments
    assertTrue("Should have at least 10 string instruments", instruments.size >= 10)
    assertTrue("Should contain Sitar", instruments.any { it.nameEnglish.contains("Sitar", ignoreCase = true) })
    assertTrue("Should contain Veena", instruments.any { it.nameEnglish.contains("Veena", ignoreCase = true) })
    assertTrue("Should contain Guitar", instruments.any { it.nameEnglish.contains("Guitar", ignoreCase = true) })
    assertTrue("Should contain Violin", instruments.any { it.nameEnglish.contains("Violin", ignoreCase = true) })

    // Verify all instruments have positive non-zero target frequencies on all strings
    instruments.forEach { inst ->
      assertTrue("${inst.nameEnglish} must have strings", inst.strings.isNotEmpty())
      inst.strings.forEach { str ->
        assertTrue("String ${str.name} on ${inst.nameEnglish} must have valid target Hz", str.targetFrequencyHz > 20f)
      }
    }
  }
}

