package app.tiebalite.core.ui.theme

import app.tiebalite.core.data.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeState(private val preferences: ThemePreferences) {
    val state: Flow<ThemeStateModel> = preferences.settings.map { prefs ->
        val seedHexValue = String.format("#%06X", prefs.seedColor and 0xFFFFFF)
        ThemeStateModel(
            themeMode = ThemeMode.valueOf(prefs.themeModeName),
            useDynamicColor = prefs.useDynamicColor,
            seedColorHex = seedHexValue
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeModeName(mode.name)
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        preferences.setDynamicColor(enabled)
    }

    suspend fun setSeedColor(hex: String) {
        val color = hex.trim().removePrefix("#").toLongOrNull(16)
        if (color != null) {
            preferences.setSeedColor(0xFF000000 or color)
        }
    }
}

data class ThemeStateModel(
    val themeMode: ThemeMode,
    val useDynamicColor: Boolean,
    val seedColorHex: String
)
