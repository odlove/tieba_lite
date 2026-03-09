package app.tiebalite.feature.thread.subposts.post

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
internal fun ThreadParentPostCard(
    item: ThreadPost,
    threadAuthorId: Long?,
    onOpenImageViewer: ((ImageViewerArgs) -> Unit)? = null,
    modifier: Modifier = Modifier,
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
            item = item,
            threadAuthorId = threadAuthorId,
        )
        ThreadPostBody(
            body = item.body,
            modifier = Modifier.padding(start = ThreadPostBodyIndent),
            onOpenImageViewer = onOpenImageViewer,
        )
    }
}
