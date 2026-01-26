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
import app.tiebalite.core.ui.components.SettingsItem
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    paddingValues: PaddingValues,
    onOpenSettings: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(layoutDirection) + 24.dp,
        end = paddingValues.calculateEndPadding(layoutDirection) + 24.dp,
        top = 10.dp,
        bottom = paddingValues.calculateBottomPadding() + 24.dp
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "我的")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = contentPadding
        ) {
            item {
                SectionHeader(text = "你好，欢迎回来")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item {
                SectionHeader(text = "常用入口")
                SettingsItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null
                        )
                    },
                    title = { Text(text = "收藏") },
                    subtitle = { Text(text = "12 条") },
                    onClick = {}
                )
                HorizontalDivider(
                    // modifier = Modifier.padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null
                        )
                    },
                    title = { Text(text = "历史") },
                    subtitle = { Text(text = "5 天") },
                    onClick = {}
                )
                // HorizontalDivider(
                //     modifier = Modifier.padding(top = 10.dp),
                //     color = MaterialTheme.colorScheme.outlineVariant
                // )
            }

            item {
                SectionHeader(text = "设置")
                SettingsItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null
                        )
                    },
                    title = { Text(text = "设置") },
                    subtitle = { Text(text = "隐私、通知与更多选项") },
                    onClick = onOpenSettings
                )
            }
        }
    }
}

