package app.tiebalite.feature.thread.ui.post.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.thread.ThreadPostBody
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
internal fun PostImageGrid(images: List<ThreadPostBody.MediaPart.Image>) {
    if (images.isEmpty()) {
        return
    }
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val containerWidthDp = with(LocalDensity.current) { containerWidthPx.toDp() }
    val widthFraction = if (containerWidthDp < 600.dp) 1f else 0.5f
    Column(
        modifier = Modifier.fillMaxWidth(widthFraction),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        images.forEach { image ->
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(image.url)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio = image.aspectRatioOrDefault())
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun ThreadPostBody.MediaPart.Image.aspectRatioOrDefault(defaultRatio: Float = 1f): Float {
    val imageWidth = width ?: return defaultRatio
    val imageHeight = height ?: return defaultRatio
    if (imageWidth <= 0 || imageHeight <= 0) {
        return defaultRatio
    }
    return imageWidth.toFloat() / imageHeight.toFloat()
}
