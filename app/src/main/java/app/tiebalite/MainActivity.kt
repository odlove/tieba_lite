package app.tiebalite

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.tiebalite.core.data.theme.ThemePreferences
import app.tiebalite.core.ui.theme.state.ThemeState
import app.tiebalite.core.ui.theme.state.UiThemeMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreferences = ThemePreferences(this)
        val themeState = ThemeState(themePreferences, lifecycleScope)
        applyEdgeToEdge(
            darkTheme = when (themeState.state.value.themeMode) {
                UiThemeMode.Dark -> true
                UiThemeMode.Light -> false
                UiThemeMode.System -> resources.configuration.isSystemInDarkTheme
            }
        )

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    themeState.state.map { state ->
                        state.themeMode
                    },
                    isSystemInDarkTheme()
                ) { themeMode, systemDarkTheme ->
                    when (themeMode) {
                        UiThemeMode.Dark -> true
                        UiThemeMode.Light -> false
                        UiThemeMode.System -> systemDarkTheme
                    }
                }
                    .distinctUntilChanged()
                    .collect { darkTheme ->
                        applyEdgeToEdge(darkTheme)
                    }
            }
        }

        setContent {
            TiebaliteApp(themeState = themeState)
        }
    }

    private fun applyEdgeToEdge(darkTheme: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = lightScrim,
                darkScrim = darkScrim
            ) { darkTheme }
        )
    }
}

private val Configuration.isSystemInDarkTheme: Boolean
    get() = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

private fun ComponentActivity.isSystemInDarkTheme() = callbackFlow {
    channel.trySend(resources.configuration.isSystemInDarkTheme)

    val listener = Consumer<Configuration> { configuration ->
        channel.trySend(configuration.isSystemInDarkTheme)
    }
    addOnConfigurationChangedListener(listener)

    awaitClose { removeOnConfigurationChangedListener(listener) }
}
    .distinctUntilChanged()
    .conflate()

private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
