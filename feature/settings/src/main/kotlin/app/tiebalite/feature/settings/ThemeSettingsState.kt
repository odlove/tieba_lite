package app.tiebalite.feature.settings

import app.tiebalite.core.ui.theme.DefaultSeedColorHex
import app.tiebalite.core.ui.theme.ThemeMode

data class ThemeSettingsState(
    val themeMode: ThemeMode = ThemeMode.Light,
    val useDynamicColor: Boolean = true,
    val seedColorHex: String = DefaultSeedColorHex
)
