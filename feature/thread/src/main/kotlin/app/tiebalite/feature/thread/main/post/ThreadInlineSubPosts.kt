package app.tiebalite.feature.thread.main.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.feature.thread.common.post.ThreadPostRichText

@Composable
internal fun ThreadInlineSubPosts(
    post: ThreadPost,
    threadAuthorId: Long?,
    onOpenSubPosts: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (post.subPosts.isEmpty()) {
        return
    }

    val remainingCount = (post.subPostCount - post.subPosts.size).coerceAtLeast(0)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(vertical = 4.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
    ) {
        post.subPosts.forEach { subPost ->
            ThreadInlineSubPostItem(
                item = subPost,
                threadAuthorId = threadAuthorId,
                onClick = { onOpenSubPosts(post.id) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
            )
        }

        if (remainingCount > 0) {
            ThreadPostRichText(
                inline = emptyList(),
                suffix = AnnotatedString("还有 $remainingCount 条回复"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                onClick = { onOpenSubPosts(post.id) },
            )
        }
    }
}
