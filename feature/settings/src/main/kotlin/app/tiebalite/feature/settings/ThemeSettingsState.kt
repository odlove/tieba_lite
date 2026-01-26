package app.tiebalite.feature.settings

import app.tiebalite.core.ui.theme.state.UiThemeMode
import app.tiebalite.core.data.theme.ThemeDefaults

data class ThemeSettingsState(
    val themeMode: UiThemeMode = UiThemeMode.System,
    val useDynamicColor: Boolean = true,
    val seedColorHex: String = String.format("#%06X", ThemeDefaults.settings.seedColor and 0xFFFFFF)
)
