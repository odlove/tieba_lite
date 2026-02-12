package app.tiebalite.core.model.theme

data class ThemeSettings(
    val themeMode: ThemeMode,
    val useDynamicColor: Boolean,
    val seedColor: Long,
)
