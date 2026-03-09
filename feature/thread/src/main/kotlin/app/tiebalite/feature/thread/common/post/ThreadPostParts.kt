package app.tiebalite.feature.thread.common.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.core.model.thread.ThreadPostBody

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
        Text(
            text = "${item.floor}楼",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun ThreadPostBody(
    body: ThreadPostBody,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ThreadPostContentSection(
            body = body,
        )
    }
}
