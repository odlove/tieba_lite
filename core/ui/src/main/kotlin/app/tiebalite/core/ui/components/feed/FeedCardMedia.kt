package app.tiebalite.core.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.tiebalite.core.data.recommend.model.RecommendItem
import coil3.compose.AsyncImage

@Composable
internal fun FeedCardMedia(item: RecommendItem) {
    val cover = item.coverImageUrl?.trim().orEmpty()
    if (cover.isBlank()) {
        return
    }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.85f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = cover,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(1.85f),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f)),
        )
    }
}
