package app.tiebalite.feature.settings

import app.tiebalite.core.model.theme.ThemeDefaults
import app.tiebalite.core.model.theme.ThemeMode

data class ThemeSettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val useDynamicColor: Boolean = true,
    val seedColorHex: String = String.format("#%06X", ThemeDefaults.settings.seedColor and 0xFFFFFF),
)
