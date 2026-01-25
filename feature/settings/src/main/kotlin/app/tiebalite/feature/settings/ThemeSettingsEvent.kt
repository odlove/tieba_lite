package app.tiebalite.feature.settings

import app.tiebalite.core.ui.theme.state.ThemeMode

sealed interface ThemeSettingsEvent {
    data class SetThemeMode(val mode: ThemeMode) : ThemeSettingsEvent
    data class SetDynamicColor(val enabled: Boolean) : ThemeSettingsEvent
    data class SetSeedColor(val value: String) : ThemeSettingsEvent
}
