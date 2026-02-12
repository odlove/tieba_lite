package app.tiebalite.core.data.theme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.tiebalite.core.model.theme.ThemeDefaults
import app.tiebalite.core.model.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemePreferencesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun defaultsMatchThemeDefaults() {
        val preferences = ThemePreferences(context)

        assertEquals(ThemeDefaults.settings, preferences.settings.value)
    }

    @Test
    fun updatesPersistAcrossInstances() {
        val preferences = ThemePreferences(context)
        preferences.setThemeMode(ThemeMode.Dark)
        preferences.setDynamicColor(false)
        preferences.setSeedColor(0xFF112233)

        val reloaded = ThemePreferences(context)
        val settings = reloaded.settings.value

        assertEquals(ThemeMode.Dark, settings.themeMode)
        assertEquals(false, settings.useDynamicColor)
        assertEquals(0xFF112233, settings.seedColor)
    }

    @Test
    fun missingKeysFallbackToDefaults() {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(ThemePreferencesKeys.themeMode, ThemeMode.Light.name)
            .commit()

        val preferences = ThemePreferences(context)
        val settings = preferences.settings.value

        assertEquals(ThemeMode.Light, settings.themeMode)
        assertEquals(ThemeDefaults.settings.useDynamicColor, settings.useDynamicColor)
        assertEquals(ThemeDefaults.settings.seedColor, settings.seedColor)
    }
}

private const val PREFERENCES_NAME = "theme_settings"
