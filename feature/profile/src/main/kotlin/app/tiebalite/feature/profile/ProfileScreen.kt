package app.tiebalite.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SectionHeader
import app.tiebalite.core.ui.components.SettingsListItem
import app.tiebalite.core.ui.theme.Spacing

@Composable
fun ProfileScreen(
    paddingValues: PaddingValues,
    onOpenSettings: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(layoutDirection) + Spacing.lg,
        end = paddingValues.calculateEndPadding(layoutDirection) + Spacing.lg,
        top = Spacing.sm,
        bottom = paddingValues.calculateBottomPadding() + Spacing.lg
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "我的")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = contentPadding
        ) {
            item {
                SectionHeader(text = "你好，欢迎回来")
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Text(
                            text = "未命名用户",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "浏览 12 天 · 已关注 8 个话题",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = Spacing.sm), color = MaterialTheme.colorScheme.outlineVariant)
            }

            item {
                SectionHeader(text = "常用入口")
                SettingsListItem(
                    title = "收藏",
                    description = "12 条",
                    leading = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                    onClick = {}
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsListItem(
                    title = "历史",
                    description = "5 天",
                    leading = { Icon(Icons.Outlined.History, contentDescription = null) },
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(top = Spacing.sm), color = MaterialTheme.colorScheme.outlineVariant)
            }

            item {
                SectionHeader(text = "设置")
                SettingsListItem(
                    title = "设置",
                    description = "隐私、通知与更多选项",
                    leading = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    onClick = onOpenSettings
                )
            }
        }
    }
}
