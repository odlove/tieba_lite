package app.tiebalite.core.ui.theme.runtime

import android.os.Build
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.tiebalite.core.ui.theme.state.UiThemeMode
import app.tiebalite.core.ui.theme.tokens.Typography
import app.tiebalite.core.data.theme.ThemeDefaults

@Composable
fun TiebaliteTheme(
    themeMode: UiThemeMode = UiThemeMode.System,
    useDynamicColor: Boolean = true,
    seedColorHex: String = String.format("#%06X", ThemeDefaults.settings.seedColor and 0xFFFFFF),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = when (themeMode) {
        UiThemeMode.Dark -> true
        UiThemeMode.Light -> false
        UiThemeMode.System -> isSystemInDarkTheme()
    }

    val colors = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val seedColor = run {
                val cleaned = seedColorHex.trim().removePrefix("#")
                if (cleaned.length != 6) {
                    null
                } else {
                    cleaned.toLongOrNull(16)?.let { Color(0xFF000000 or it) }
                }
            } ?: Color(ThemeDefaults.settings.seedColor)
            colorSchemeFromSeed(seedColor, isDark)
        }
    }

    val animatedColors = animateColorScheme(colors, tween(420))

    MaterialTheme(
        colorScheme = animatedColors,
        typography = Typography,
        content = content
    )
}
