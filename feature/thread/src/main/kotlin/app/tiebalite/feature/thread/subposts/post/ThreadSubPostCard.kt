package app.tiebalite.feature.thread.subposts.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.core.model.thread.ThreadSubPost
import app.tiebalite.feature.thread.common.post.AuthorAvatar
import app.tiebalite.feature.thread.common.post.AuthorNameWithLevel
import app.tiebalite.feature.thread.common.post.ThreadPostContentSection
import app.tiebalite.feature.thread.common.post.formatPostMeta

@Composable
internal fun ThreadSubPostCard(
    item: ThreadSubPost,
    threadAuthorId: Long?,
    onOpenImageViewer: ((ImageViewerArgs) -> Unit)? = null,
) {
    val isThreadAuthor = threadAuthorId != null && threadAuthorId > 0L && item.authorId == threadAuthorId
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AuthorAvatar(
                name = item.authorName,
                imageUrl = item.authorAvatarUrl,
                size = 32.dp,
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
        }
        ThreadPostContentSection(
            body = item.body,
            onOpenImageViewer = onOpenImageViewer,
        )
    }
}
