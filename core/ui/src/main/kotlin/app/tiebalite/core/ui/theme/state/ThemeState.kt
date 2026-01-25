package app.tiebalite.core.ui.theme.state

import app.tiebalite.core.data.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeState(private val preferences: ThemePreferences) {
    val state: Flow<ThemeStateModel> = preferences.settings.map { prefs ->
        ThemeStateModel(
            themeMode = ThemeMode.valueOf(prefs.themeModeName),
            useDynamicColor = prefs.useDynamicColor,
            seedColorHex = formatSeedHex(prefs.seedColor)
        )
    }

    fun currentState(): ThemeStateModel {
        val settings = preferences.currentSettings()
        return ThemeStateModel(
            themeMode = ThemeMode.valueOf(settings.themeModeName),
            useDynamicColor = settings.useDynamicColor,
            seedColorHex = formatSeedHex(settings.seedColor)
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeModeName(mode.name)
    }

    fun setDynamicColor(enabled: Boolean) {
        preferences.setDynamicColor(enabled)
    }

    fun setSeedColor(hex: String) {
        val color = hex.trim().removePrefix("#").toLongOrNull(16)
        if (color != null) {
            preferences.setSeedColor(0xFF000000 or color)
        }
    }

    private fun formatSeedHex(seedColor: Long): String {
        return String.format("#%06X", seedColor and 0xFFFFFF)
    }
}

data class ThemeStateModel(
    val themeMode: ThemeMode,
    val useDynamicColor: Boolean,
    val seedColorHex: String
)
