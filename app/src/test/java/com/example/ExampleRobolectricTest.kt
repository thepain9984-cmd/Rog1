package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProfileRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("ConsoleHUD Gamepad", appName)
  }

  @Test
  fun `verify default profiles loaded`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = ProfileRepository(context)
    val profiles = repo.profiles.value
    assertTrue("Should contain preset profiles", profiles.isNotEmpty())
    val active = repo.getActiveProfile()
    assertNotNull(active)
    assertTrue("Active profile should have buttons", active.buttons.isNotEmpty())
  }
}
