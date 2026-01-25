package app.tiebalite.core.ui.system

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun ApplySystemBars() {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme
    val useDarkIcons = colorScheme.background.luminance() > 0.5f

    DisposableEffect(view) {
        val activity = view.context.findActivity() ?: return@DisposableEffect onDispose {}
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        onDispose {}
    }

    SideEffect {
        val activity = view.context.findActivity() ?: return@SideEffect
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = useDarkIcons
        controller.isAppearanceLightNavigationBars = useDarkIcons
    }
}

// @Composable
// fun SystemBarsBackground(
//     color: Color = MaterialTheme.colorScheme.background,
//     modifier: Modifier = Modifier
// ) {
//     Box(modifier = modifier.fillMaxSize()) {
//         Box(
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .windowInsetsTopHeight(WindowInsets.safeDrawing)
//                 .background(color)
//                 .align(Alignment.TopStart)
//         )
//         Box(
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .windowInsetsBottomHeight(WindowInsets.safeDrawing)
//                 .background(color)
//                 .align(Alignment.BottomStart)
//         )
//     }
// }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
