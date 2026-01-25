package app.tiebalite.core.ui.theme.state

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.tiebalite.core.data.ThemePreferences

@Composable
fun rememberThemeState(context: Context): ThemeState {
    return remember(context) { ThemeState(ThemePreferences(context)) }
}
