package app.tiebalite.core.ui.components.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.ui.R

@Composable
internal fun FeedCardActions(item: RecommendItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
    ) {
        ActionStat(
            icon = Icons.Rounded.Share,
            text = statText(item.shareCount, "分享"),
            modifier = Modifier.weight(1f),
        )
        ActionStat(
            icon = ImageVector.vectorResource(id = R.drawable.ic_comment_new),
            text = statText(item.replyCount.toLong(), "回复"),
            modifier = Modifier.weight(1f),
        )
        ActionStat(
            icon = Icons.Rounded.FavoriteBorder,
            text = statText(item.agreeCount.toLong(), "赞"),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionStat(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun statText(value: Long, fallback: String): String {
    if (value <= 0) {
        return fallback
    }
    return when {
        value >= 10_000 -> "${"%.1f".format(value / 10_000f)}万"
        else -> value.toString()
    }
}
