package app.tiebalite.feature.settings

import app.tiebalite.core.ui.theme.state.UiThemeMode

sealed interface ThemeSettingsEvent {
    data class SetThemeMode(val mode: UiThemeMode) : ThemeSettingsEvent
    data class SetDynamicColor(val enabled: Boolean) : ThemeSettingsEvent
    data class SetSeedColor(val value: String) : ThemeSettingsEvent
}
