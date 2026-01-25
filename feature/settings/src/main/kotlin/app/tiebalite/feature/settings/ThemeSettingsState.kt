package app.tiebalite.feature.settings

import app.tiebalite.core.ui.theme.state.DefaultSeedColorHex
import app.tiebalite.core.ui.theme.state.ThemeMode

data class ThemeSettingsState(
    val themeMode: ThemeMode = ThemeMode.Light,
    val useDynamicColor: Boolean = true,
    val seedColorHex: String = DefaultSeedColorHex
)
