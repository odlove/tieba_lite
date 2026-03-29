package app.tiebalite.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SettingsItem
import app.tiebalite.core.ui.components.SettingsItemIcon
import app.tiebalite.core.ui.components.SettingsItemStyle

@Composable
fun ProfileScreen(
    paddingValues: PaddingValues,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
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
        AppTopBar(title = "我的")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item {
                SettingsItem(
                    style = SettingsItemStyle.WideLeading,
                    leadingContent = {
                        SettingsItemIcon(
                            imageVector = Icons.Outlined.History,
                            style = SettingsItemStyle.WideLeading,
                        )
                    },
                    title = { Text(text = "浏览历史") },
                    subtitle = { Text(text = "查看最近访问过的帖子") },
                    onClick = onOpenHistory,
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
                            imageVector = Icons.Outlined.Settings,
                            style = SettingsItemStyle.WideLeading,
                        )
                    },
                    title = { Text(text = "设置") },
                    subtitle = { Text(text = "账号、主题与更多选项") },
                    onClick = onOpenSettings,
                )
                HorizontalDivider(
                    modifier = dividerPadding,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}
