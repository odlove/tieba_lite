package app.tiebalite.core.ui.theme.state

import app.tiebalite.core.data.theme.ThemeMode

enum class UiThemeMode {
    System,
    Light,
    Dark
}

fun String.toUiThemeMode(): UiThemeMode {
    return when (this) {
        ThemeMode.System -> UiThemeMode.System
        ThemeMode.Dark -> UiThemeMode.Dark
        ThemeMode.Light -> UiThemeMode.Light
        else -> UiThemeMode.System
    }
}

fun UiThemeMode.toStorageValue(): String {
    return when (this) {
        UiThemeMode.System -> ThemeMode.System
        UiThemeMode.Light -> ThemeMode.Light
        UiThemeMode.Dark -> ThemeMode.Dark
    }
}
