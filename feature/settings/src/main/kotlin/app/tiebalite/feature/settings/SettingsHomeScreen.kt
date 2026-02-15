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
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SettingsItem
import app.tiebalite.feature.settings.account.SettingsAccountViewModel
import androidx.compose.ui.unit.dp
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
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(layoutDirection) + 24.dp,
        end = paddingValues.calculateEndPadding(layoutDirection) + 24.dp,
        top = 10.dp,
        bottom = paddingValues.calculateBottomPadding() + 24.dp
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.settings_home_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = contentPadding
        ) {
            item {
                SettingsItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
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
                    onClick = onOpenTheme,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
