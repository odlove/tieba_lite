package app.tiebalite.feature.settings.account

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SettingsItem
import app.tiebalite.feature.settings.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun SettingsAccountDetailScreen(
    paddingValues: PaddingValues,
    account: SettingsAccountItem?,
    onSwitchAccount: () -> Unit,
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
            title = stringResource(R.string.settings_account_detail_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = contentPadding,
        ) {
            if (account == null) {
                item {
                    Text(
                        text = stringResource(R.string.settings_account_detail_not_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                if (!account.isActive) {
                    item {
                        SettingsItem(
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            title = { Text(text = stringResource(R.string.settings_account_detail_set_active_title)) },
                            subtitle = { Text(text = stringResource(R.string.settings_account_detail_set_active_desc)) },
                            onClick = onSwitchAccount,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }

                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_status),
                        value = account.isActive.toString(),
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_session_valid),
                        value = account.isSessionValid.toString(),
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_account_id),
                        value = account.accountId,
                        monospace = true,
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_display_name),
                        value = account.displayName.orDash(),
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_user_name),
                        value = account.userName.orDash(),
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_user_id),
                        value = account.userId.orDash(),
                        monospace = true,
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_avatar_url),
                        value = account.avatarUrl.orDash(),
                        monospace = true,
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_bduss),
                        value = account.bduss,
                        monospace = true,
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_stoken),
                        value = account.stoken,
                        monospace = true,
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_tbs),
                        value = account.tbs.orDash(),
                        monospace = true,
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_cookie),
                        value = account.rawCookie.orDash(),
                        monospace = true,
                    )
                }
                item {
                    DetailField(
                        label = stringResource(R.string.settings_account_detail_updated_at),
                        value = formatUpdatedAt(account.updatedAtMillis),
                        monospace = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    monospace: Boolean = false,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val copyLabel = stringResource(R.string.settings_account_detail_copy)
    val copiedMessagePrefix = stringResource(R.string.settings_account_detail_copied)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClickLabel = copyLabel,
                    onLongClick = {
                        scope.launch {
                            clipboard.setClipEntry(
                                ClipData
                                    .newPlainText("text", value)
                                    .toClipEntry(),
                            )
                            Toast
                                .makeText(
                                    context,
                                    "$copiedMessagePrefix $label",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    },
                ).padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = if (monospace) FontFamily.Monospace else null,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

private fun String?.orDash(): String =
    if (this.isNullOrBlank()) {
        "-"
    } else {
        this
    }

private fun formatUpdatedAt(updatedAtMillis: Long): String {
    if (updatedAtMillis <= 0L) {
        return "-"
    }
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(updatedAtMillis))
}
