package app.tiebalite.feature.thread.main.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.feature.thread.common.post.ThreadPostBody
import app.tiebalite.feature.thread.common.post.ThreadPostBodyIndent
import app.tiebalite.feature.thread.common.post.ThreadPostHeader
import app.tiebalite.feature.thread.common.post.ThreadPostHorizontalPadding
import app.tiebalite.feature.thread.common.post.ThreadPostVerticalPadding

@Composable
internal fun ThreadReplyListItem(
    item: ThreadPost,
    threadAuthorId: Long?,
    modifier: Modifier = Modifier,
    onOpenSubPosts: (Long) -> Unit,
    onOpenImageViewer: ((ImageViewerArgs) -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThreadPostHorizontalPadding,
                    vertical = ThreadPostVerticalPadding,
                ),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ThreadPostHeader(
            authorId = item.authorId,
            authorName = item.authorName,
            authorLevel = item.authorLevel,
            authorAvatarUrl = item.authorAvatarUrl,
            ipLocation = item.ipLocation,
            timestampSeconds = item.timestampSeconds,
            agreeCount = item.agreeCount,
            floor = item.floor,
            threadAuthorId = threadAuthorId,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = ThreadPostBodyIndent),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThreadPostBody(
                body = item.body,
                onOpenImageViewer = onOpenImageViewer,
            )
            ThreadInlineSubPosts(
                post = item,
                threadAuthorId = threadAuthorId,
                onOpenSubPosts = onOpenSubPosts,
            )
        }
    }
}
