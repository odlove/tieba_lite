package app.tiebalite.core.ui.system

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun ApplySystemBars() {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme
    val useDarkIcons = colorScheme.background.luminance() > 0.5f

    SideEffect {
        val activity = view.context.findActivity() ?: return@SideEffect
        activity.window.statusBarColor = colorScheme.background.toArgb()
        activity.window.navigationBarColor = colorScheme.background.toArgb()

        val controller = WindowInsetsControllerCompat(activity.window, view)
        controller.isAppearanceLightStatusBars = useDarkIcons
        controller.isAppearanceLightNavigationBars = useDarkIcons
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
