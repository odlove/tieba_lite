package app.tiebalite.core.ui.components.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.recommend.RecommendItem

@Composable
fun FeedCard(
    item: RecommendItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onOpenForum: ((String) -> Unit)? = null,
    onOpenMedia: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .optionalClickable(onClick = onClick)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FeedCardHeader(
            item = item,
            onOpenForum = onOpenForum,
        )
        FeedCardBody(item = item)
        FeedCardMedia(
            item = item,
            onClick = onOpenMedia,
        )
        FeedCardActions(item = item)
    }
}

private fun Modifier.optionalClickable(
    onClick: (() -> Unit)?,
): Modifier = if (onClick == null) this else clickable(onClick = onClick)
