package app.tiebalite.core.data.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import app.tiebalite.core.model.theme.ThemeDefaults
import app.tiebalite.core.model.theme.ThemeMode
import app.tiebalite.core.model.theme.ThemeSettings
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    private val mutableSettings = MutableStateFlow(readSettings())
    val settings: StateFlow<ThemeSettings> = mutableSettings.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        preferences.edit {
            putString(ThemePreferencesKeys.themeMode, mode.name)
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
        val themeMode =
            preferences.getString(ThemePreferencesKeys.themeMode, defaults.themeMode.name)
                ?.let(::parseThemeMode)
                ?: defaults.themeMode
        val useDynamicColor =
            preferences.getBoolean(ThemePreferencesKeys.dynamicColor, defaults.useDynamicColor)
        val seedColor =
            preferences.getLong(ThemePreferencesKeys.seedColor, defaults.seedColor)
        return ThemeSettings(
            themeMode = themeMode,
            useDynamicColor = useDynamicColor,
            seedColor = seedColor
        )
    }
}

private const val PreferencesName = "theme_settings"

private fun parseThemeMode(raw: String): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.System
