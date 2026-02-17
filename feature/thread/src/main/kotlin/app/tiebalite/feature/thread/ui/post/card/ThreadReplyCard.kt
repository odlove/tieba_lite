package app.tiebalite.feature.thread.ui.post.card

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
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.feature.thread.ui.post.content.ThreadPostContentSection
import app.tiebalite.feature.thread.ui.post.shared.AuthorNameWithLevel

@Composable
internal fun ThreadReplyCard(
    item: ThreadPost,
) {
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
        ) {
            AuthorNameWithLevel(
                name = item.authorName ?: "贴吧用户",
                level = item.authorLevel,
                textStyle = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${item.floor}楼",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ThreadPostContentSection(
            body = item.body,
        )
    }
}
