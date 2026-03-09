package app.tiebalite.feature.thread.main.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.thread.ThreadPost

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
            Text(
                text = "还有 $remainingCount 条回复",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSubPosts(post.id) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
