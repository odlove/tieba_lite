package app.tiebalite.feature.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SettingsItem
import app.tiebalite.feature.settings.R

data class SettingsAccountItem(
    val accountId: String,
    val title: String,
    val subtitle: String,
    val isActive: Boolean,
    val isSessionValid: Boolean,
    val bduss: String,
    val stoken: String,
    val tbs: String?,
    val rawCookie: String?,
    val userId: String?,
    val userName: String?,
    val displayName: String?,
    val avatarUrl: String?,
    val updatedAtMillis: Long,
)

@Composable
fun SettingsAccountScreen(
    paddingValues: PaddingValues,
    isLoggedIn: Boolean,
    accounts: List<SettingsAccountItem>,
    onOpenWebLogin: () -> Unit,
    onOpenCredentialLogin: () -> Unit,
    onOpenAccountDetail: (String) -> Unit,
    onRemoveAccount: (String) -> Unit,
    onLogoutActive: () -> Unit,
    onBack: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(layoutDirection) + 24.dp,
        end = paddingValues.calculateEndPadding(layoutDirection) + 24.dp,
        top = 10.dp,
        bottom = paddingValues.calculateBottomPadding() + 24.dp,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.settings_account_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = contentPadding,
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
                    title = { Text(text = stringResource(R.string.settings_web_login_entry_title)) },
                    subtitle = {
                        Text(
                            text =
                                stringResource(
                                    if (isLoggedIn) {
                                        R.string.settings_web_login_entry_desc_logged_in
                                    } else {
                                        R.string.settings_web_login_entry_desc_logged_out
                                    },
                                ),
                        )
                    },
                    onClick = onOpenWebLogin,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                SettingsItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    title = { Text(text = stringResource(R.string.settings_credential_login_entry_title)) },
                    subtitle = { Text(text = stringResource(R.string.settings_credential_login_entry_desc)) },
                    onClick = onOpenCredentialLogin,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (accounts.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.settings_account_list_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(
                items = accounts,
                key = { item -> item.accountId },
            ) { account ->
                SettingsItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = null,
                            tint =
                                if (account.isActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    },
                    title = { Text(text = account.title) },
                    subtitle = { Text(text = account.subtitle) },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (account.isActive) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = stringResource(R.string.settings_account_active),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            IconButton(
                                onClick = { onRemoveAccount(account.accountId) },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = stringResource(R.string.settings_account_remove),
                                )
                            }
                        }
                    },
                    onClick = {
                        onOpenAccountDetail(account.accountId)
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (isLoggedIn) {
                item {
                    SettingsItem(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        title = { Text(text = stringResource(R.string.settings_logout_entry_title)) },
                        subtitle = { Text(text = stringResource(R.string.settings_logout_entry_desc)) },
                        onClick = onLogoutActive,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
