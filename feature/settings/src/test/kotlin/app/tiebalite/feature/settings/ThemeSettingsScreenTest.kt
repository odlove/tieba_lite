package app.tiebalite.feature.settings

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import app.tiebalite.core.model.theme.ThemeDefaults
import app.tiebalite.core.model.theme.ThemeMode
import app.tiebalite.core.ui.theme.runtime.TiebaliteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30, 34], qualifiers = "zh-rCN-w320dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeSettingsScreenTest {
    @get:Rule
    val compose = createComposeRule()
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun selectionsApplyAndKeepCustomColor() {
        var settings by mutableStateOf(ThemeDefaults.settings)
        compose.setContent {
            TiebaliteTheme(settings.themeMode, settings.useDynamicColor, settings.seedColor) {
                ThemeSettingsScreen(
                    paddingValues = PaddingValues(),
                    state = settings,
                    onEvent = {
                        settings = when (it) {
                            is ThemeSettingsEvent.SetThemeMode -> settings.copy(themeMode = it.mode)
                            is ThemeSettingsEvent.SetDynamicColor -> settings.copy(useDynamicColor = it.enabled)
                            is ThemeSettingsEvent.SetSeedColor -> settings.copy(seedColor = it.value)
                        }
                    },
                    onBack = {},
                )
            }
        }

        compose.onNodeWithText(context.getString(R.string.settings_dark)).performClick().assertIsSelected()
        compose.onNodeWithText(context.getString(R.string.settings_system)).assertIsNotSelected()
        compose.runOnIdle { assertEquals(ThemeMode.Dark, settings.themeMode) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            compose.onNodeWithContentDescription(context.getString(R.string.settings_seed_blue)).assertDoesNotExist()
            compose.onNodeWithText(context.getString(R.string.settings_dynamic_color)).assertIsSelected()
            compose.onNodeWithText(context.getString(R.string.settings_custom_color)).performClick().assertIsSelected()
            compose.onNodeWithText(context.getString(R.string.settings_dynamic_color)).assertIsNotSelected()
        } else {
            compose.onNodeWithText(context.getString(R.string.settings_dynamic_color)).assertDoesNotExist()
        }
        compose.onNodeWithText(context.getString(R.string.settings_seed_blue)).assertDoesNotExist()
        compose.onNodeWithContentDescription(context.getString(R.string.settings_seed_blue))
            .performScrollTo().performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(0xFF2F6BFFL, settings.seedColor) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            compose.onNodeWithText(context.getString(R.string.settings_dynamic_color))
                .performScrollTo().performClick().assertIsSelected()
            compose.onNodeWithContentDescription(context.getString(R.string.settings_seed_blue)).assertDoesNotExist()
            compose.onNodeWithText(context.getString(R.string.settings_custom_color)).performClick().assertIsSelected()
            compose.onNodeWithContentDescription(context.getString(R.string.settings_seed_blue)).performScrollTo().assertIsSelected()
        }
    }

}
