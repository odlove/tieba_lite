package app.tiebalite.feature.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SettingsItem
import app.tiebalite.core.ui.components.SettingsItemIcon
import app.tiebalite.core.ui.components.SettingsItemStyle
import app.tiebalite.feature.settings.R
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

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
    var pendingRemovalAccount by remember { mutableStateOf<SettingsAccountItem?>(null) }
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
            title = stringResource(R.string.settings_account_title),
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
                            imageVector = Icons.Outlined.PersonOutline,
                            style = SettingsItemStyle.WideLeading,
                        )
                    },
                    title = { Text(text = stringResource(R.string.settings_credential_login_entry_title)) },
                    subtitle = { Text(text = stringResource(R.string.settings_credential_login_entry_desc)) },
                    onClick = onOpenCredentialLogin,
                )
                HorizontalDivider(
                    modifier = dividerPadding,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            if (accounts.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.settings_account_list_title),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
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
                    style = SettingsItemStyle.WideLeading,
                    leadingContent = {
                        AccountAvatar(account = account)
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
                                onClick = { pendingRemovalAccount = account },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = stringResource(R.string.settings_account_remove),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    },
                    onClick = {
                        onOpenAccountDetail(account.accountId)
                    },
                )
                HorizontalDivider(
                    modifier = dividerPadding,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            if (isLoggedIn) {
                item {
                    SettingsItem(
                        style = SettingsItemStyle.WideLeading,
                        leadingContent = {
                            SettingsItemIcon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                style = SettingsItemStyle.WideLeading,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        title = { Text(text = stringResource(R.string.settings_logout_entry_title)) },
                        subtitle = { Text(text = stringResource(R.string.settings_logout_entry_desc)) },
                        onClick = onLogoutActive,
                    )
                    HorizontalDivider(
                        modifier = dividerPadding,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }

        val accountToRemove = pendingRemovalAccount
        if (accountToRemove != null) {
            AlertDialog(
                onDismissRequest = { pendingRemovalAccount = null },
                title = { Text(text = stringResource(R.string.settings_account_remove_confirm_title)) },
                text = {
                    Text(
                        text =
                            stringResource(
                                R.string.settings_account_remove_confirm_message,
                                accountToRemove.title,
                            ),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onRemoveAccount(accountToRemove.accountId)
                            pendingRemovalAccount = null
                        },
                    ) {
                        Text(text = stringResource(R.string.settings_account_remove))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingRemovalAccount = null }) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun AccountAvatar(account: SettingsAccountItem) {
    val avatarUrl = account.avatarUrl?.trim().orEmpty()
    if (avatarUrl.isNotEmpty()) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        return
    }

    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        SettingsItemIcon(
            imageVector = Icons.Outlined.PersonOutline,
            style = SettingsItemStyle.Standard,
            tint =
                if (account.isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}
