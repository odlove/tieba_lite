package app.tiebalite.core.data.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    private val mutableSettings = MutableStateFlow(readSettings())
    val settings: StateFlow<ThemeSettings> = mutableSettings.asStateFlow()

    fun setThemeMode(mode: String) {
        preferences.edit {
            putString(ThemePreferencesKeys.themeMode, mode)
        }
        mutableSettings.value = mutableSettings.value.copy(themeMode = mode)
    }

    fun setDynamicColor(enabled: Boolean) {
        preferences.edit {
            putBoolean(ThemePreferencesKeys.dynamicColor, enabled)
        }
        mutableSettings.value = mutableSettings.value.copy(useDynamicColor = enabled)
    }

    fun setSeedColor(value: Long) {
        preferences.edit {
            putLong(ThemePreferencesKeys.seedColor, value)
        }
        mutableSettings.value = mutableSettings.value.copy(seedColor = value)
    }

    private fun readSettings(): ThemeSettings {
        val defaults = ThemeDefaults.settings
        val themeModeName =
            preferences.getString(ThemePreferencesKeys.themeMode, defaults.themeMode)
                ?: defaults.themeMode
        val useDynamicColor =
            preferences.getBoolean(ThemePreferencesKeys.dynamicColor, defaults.useDynamicColor)
        val seedColor =
            preferences.getLong(ThemePreferencesKeys.seedColor, defaults.seedColor)
        return ThemeSettings(
            themeMode = themeModeName,
            useDynamicColor = useDynamicColor,
            seedColor = seedColor
        )
    }
}

private const val PreferencesName = "theme_settings"
