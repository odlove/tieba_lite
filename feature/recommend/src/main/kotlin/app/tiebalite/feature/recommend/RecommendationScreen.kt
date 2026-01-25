package app.tiebalite.feature.recommend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.SectionHeader
import app.tiebalite.core.ui.theme.tokens.Spacing

@Composable
fun RecommendationScreen(paddingValues: PaddingValues) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(layoutDirection) + Spacing.lg,
        end = paddingValues.calculateEndPadding(layoutDirection) + Spacing.lg,
        top = Spacing.sm,
        bottom = paddingValues.calculateBottomPadding() + Spacing.lg
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "推荐")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = contentPadding
        ) {
            item {
                SectionHeader(text = "今日精选")
                Text(
                    text = "清爽阅读，轻松发现有趣内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                FeedRow(
                    icon = Icons.Outlined.LocalFireDepartment,
                    title = "社区热帖",
                    description = "精选高互动内容，捕捉今天最热的话题。"
                )
                AssistPill(
                    text = "热度上升",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = Spacing.sm),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item {
                FeedRow(
                    icon = Icons.Outlined.PeopleAlt,
                    title = "兴趣小组",
                    description = "轻松加入话题圈，结识同好。"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    AssistPill(text = "设计")
                    AssistPill(text = "科技")
                    AssistPill(text = "生活")
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = Spacing.sm),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item {
                FeedRow(
                    icon = Icons.Outlined.AutoAwesome,
                    title = "今日推荐",
                    description = "根据你的浏览喜好，为你准备的精选列表。"
                )
                Text(
                    text = "浏览 5 分钟",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FeedRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AssistPill(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    AssistChip(
        onClick = {},
        label = { Text(text = text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        )
    )
}
