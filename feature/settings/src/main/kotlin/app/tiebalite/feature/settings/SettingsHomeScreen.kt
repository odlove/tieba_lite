package app.tiebalite.feature.settings

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
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SettingsItem
import app.tiebalite.core.ui.components.SettingsItemIcon
import app.tiebalite.core.ui.components.SettingsItemStyle
import app.tiebalite.feature.settings.account.SettingsAccountViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsHomeScreen(
    paddingValues: PaddingValues,
    isLoggedIn: Boolean,
    onOpenAccountManage: () -> Unit,
    onOpenTheme: () -> Unit,
    onBack: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            top = 12.dp,
            bottom = paddingValues.calculateBottomPadding() + 24.dp,
        )
    val dividerPadding = Modifier.padding(horizontal = 16.dp)

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.settings_home_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item {
                SettingsItem(
                    style = SettingsItemStyle.WideLeading,
                    leadingContent = {
                        SettingsItemIcon(
                            imageVector = Icons.Outlined.PersonOutline,
                            style = SettingsItemStyle.WideLeading,
                        )
                    },
                    title = { Text(text = stringResource(R.string.settings_account_entry_title)) },
                    subtitle = {
                        Text(
                            text =
                                stringResource(
                                    if (isLoggedIn) {
                                        R.string.settings_account_entry_desc_logged_in
                                    } else {
                                        R.string.settings_account_entry_desc_logged_out
                                    },
                                ),
                        )
                    },
                    onClick = onOpenAccountManage,
                )
                HorizontalDivider(
                    modifier = dividerPadding,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            item {
                SettingsItem(
                    style = SettingsItemStyle.WideLeading,
                    leadingContent = {
                        SettingsItemIcon(
                            imageVector = Icons.Outlined.Palette,
                            style = SettingsItemStyle.WideLeading,
                        )
                    },
                    title = { Text(text = stringResource(R.string.settings_theme_entry_title)) },
                    subtitle = { Text(text = stringResource(R.string.settings_theme_entry_desc)) },
                    onClick = onOpenTheme,
                )
                HorizontalDivider(
                    modifier = dividerPadding,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
fun SettingsHomeRoute(
    paddingValues: PaddingValues,
    onOpenAccountManage: () -> Unit,
    onOpenTheme: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsAccountViewModel = viewModel(factory = SettingsAccountViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsState()
    SettingsHomeScreen(
        paddingValues = paddingValues,
        isLoggedIn = state.isLoggedIn,
        onOpenAccountManage = onOpenAccountManage,
        onOpenTheme = onOpenTheme,
        onBack = onBack,
    )
}
