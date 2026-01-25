package app.tiebalite.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SettingsItem
import app.tiebalite.core.ui.theme.tokens.Spacing

@Composable
fun SettingsHomeScreen(
    paddingValues: PaddingValues,
    onOpenTheme: () -> Unit,
    onBack: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(layoutDirection) + Spacing.lg,
        end = paddingValues.calculateEndPadding(layoutDirection) + Spacing.lg,
        top = Spacing.sm,
        bottom = paddingValues.calculateBottomPadding() + Spacing.lg
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.settings_home_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = contentPadding
        ) {
            item {
                SettingsItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = { Text(text = stringResource(R.string.settings_theme_entry_title)) },
                    subtitle = { Text(text = stringResource(R.string.settings_theme_entry_desc)) },
                    onClick = onOpenTheme
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
