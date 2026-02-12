package app.tiebalite.theme

import app.tiebalite.core.data.theme.ThemePreferences
import app.tiebalite.core.model.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ThemeState(
    private val preferences: ThemePreferences,
    scope: CoroutineScope,
) {
    val state: StateFlow<ThemeStateModel> =
        preferences.settings
            .map { prefs ->
                ThemeStateModel(
                    themeMode = prefs.themeMode,
                    useDynamicColor = prefs.useDynamicColor,
                    seedColor = prefs.seedColor,
                )
            }.stateIn(
                scope,
                SharingStarted.Eagerly,
                ThemeStateModel(
                    themeMode = preferences.settings.value.themeMode,
                    useDynamicColor = preferences.settings.value.useDynamicColor,
                    seedColor = preferences.settings.value.seedColor,
                ),
            )

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

data class ThemeStateModel(
    val themeMode: ThemeMode,
    val useDynamicColor: Boolean,
    val seedColor: Long,
)
