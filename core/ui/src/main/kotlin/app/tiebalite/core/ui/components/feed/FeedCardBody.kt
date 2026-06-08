package app.tiebalite.core.ui.components.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.ui.components.text.RichText

@Composable
internal fun FeedCardBody(item: RecommendItem) {
    Column {
        RichText(
            richText = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        item.snippet?.takeIf { it.isNotBlank() }?.let { snippet ->
            RichText(
                richText = snippet,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
