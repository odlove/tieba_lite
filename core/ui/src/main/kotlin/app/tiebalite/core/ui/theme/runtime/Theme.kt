package app.tiebalite.core.ui.theme.runtime

import android.os.Build
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.tiebalite.core.ui.theme.state.DefaultSeedColorHex
import app.tiebalite.core.ui.theme.state.DefaultSeedColorLong
import app.tiebalite.core.ui.theme.state.ThemeMode
import app.tiebalite.core.ui.theme.tokens.Typography

@Composable
fun TiebaliteTheme(
    themeMode: ThemeMode = ThemeMode.Light,
    useDynamicColor: Boolean = true,
    seedColorHex: String = DefaultSeedColorHex,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = when (themeMode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }

    val colors = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val seedColor = seedColorHex.toColorOrNull() ?: Color(DefaultSeedColorLong)
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
