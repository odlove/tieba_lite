package app.tiebalite.theme

import app.tiebalite.core.data.theme.ThemePreferences
import app.tiebalite.core.model.theme.ThemeMode
import app.tiebalite.core.model.theme.ThemeSettings
import kotlinx.coroutines.flow.StateFlow

class ThemeState(
    private val preferences: ThemePreferences,
) {
    val state: StateFlow<ThemeSettings> = preferences.settings

    fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) {
        preferences.setDynamicColor(enabled)
    }

    fun setSeedColor(value: Long) {
        preferences.setSeedColor(value)
    }
}
