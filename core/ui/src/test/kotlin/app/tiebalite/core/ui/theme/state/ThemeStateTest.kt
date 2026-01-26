package app.tiebalite.core.ui.theme.state

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.tiebalite.core.data.theme.ThemeMode
import app.tiebalite.core.data.theme.ThemePreferences
import app.tiebalite.core.data.theme.ThemePreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemeStateTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun mapsPreferencesToUiState() = runTest {
        val preferences = ThemePreferences(context)
        preferences.setThemeMode(ThemeMode.Dark)
        preferences.setDynamicColor(false)
        preferences.setSeedColor(0xFF112233)

        val themeState = ThemeState(preferences, backgroundScope)
        advanceUntilIdle()

        val state = themeState.state.value
        assertEquals(UiThemeMode.Dark, state.themeMode)
        assertEquals(false, state.useDynamicColor)
        assertEquals(0xFF112233, state.seedColor)
    }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun updatesWhenChangedThroughState() = runTest {
        val preferences = ThemePreferences(context)
        val themeState = ThemeState(preferences, backgroundScope)

        themeState.setThemeMode(UiThemeMode.Light)
        themeState.setDynamicColor(false)
        themeState.setSeedColor(0xFF445566)

        val state = themeState.state.first {
            it.themeMode == UiThemeMode.Light &&
                it.useDynamicColor == false &&
                it.seedColor == 0xFF445566
        }
        assertEquals(UiThemeMode.Light, state.themeMode)
        assertEquals(false, state.useDynamicColor)
        assertEquals(0xFF445566, state.seedColor)
    }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun invalidThemeModeFallsBackToSystem() = runTest {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(ThemePreferencesKeys.themeMode, "Invalid")
            .putBoolean(ThemePreferencesKeys.dynamicColor, false)
            .putLong(ThemePreferencesKeys.seedColor, 0xFF778899)
            .commit()

        val preferences = ThemePreferences(context)
        val themeState = ThemeState(preferences, backgroundScope)
        advanceUntilIdle()

        val state = themeState.state.value
        assertEquals(UiThemeMode.System, state.themeMode)
        assertEquals(false, state.useDynamicColor)
        assertEquals(0xFF778899, state.seedColor)
    }
}

private const val PREFERENCES_NAME = "theme_settings"
