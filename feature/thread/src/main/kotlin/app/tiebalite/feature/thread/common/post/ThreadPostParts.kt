package app.tiebalite.feature.thread.common.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.core.model.thread.ThreadPostBody
import java.util.Locale

internal val ThreadPostHorizontalPadding = 16.dp
internal val ThreadPostVerticalPadding = 12.dp
internal val ThreadPostHeaderSpacing = 10.dp
internal val ThreadPostBodyIndent = 36.dp + ThreadPostHeaderSpacing

@Composable
internal fun ThreadPostHeader(
    item: ThreadPost,
    threadAuthorId: Long?,
    modifier: Modifier = Modifier,
) {
    val isThreadAuthor = threadAuthorId != null && threadAuthorId > 0L && item.authorId == threadAuthorId
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ThreadPostHeaderSpacing),
    ) {
        AuthorAvatar(
            name = item.authorName,
            imageUrl = item.authorAvatarUrl,
            size = 36.dp,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AuthorNameWithLevel(
                name = item.authorName ?: "贴吧用户",
                level = item.authorLevel,
                isThreadAuthor = isThreadAuthor,
                textStyle = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = formatPostMeta(item.timestampSeconds, item.ipLocation),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${item.floor}楼",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ThreadAgreeStat(agreeCount = item.agreeCount)
        }
    }
}

@Composable
internal fun ThreadPostBody(
    body: ThreadPostBody,
    modifier: Modifier = Modifier,
    onOpenImageViewer: ((ImageViewerArgs) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ThreadPostContentSection(
            body = body,
            onOpenImageViewer = onOpenImageViewer,
        )
    }
}

@Composable
internal fun ThreadAgreeStat(
    agreeCount: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatAgreeCount(agreeCount),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatAgreeCount(value: Long): String {
    if (value <= 0) {
        return "0"
    }
    return when {
        value >= 10_000 -> String.format(Locale.US, "%.1f万", value / 10_000f)
        else -> value.toString()
    }
}
