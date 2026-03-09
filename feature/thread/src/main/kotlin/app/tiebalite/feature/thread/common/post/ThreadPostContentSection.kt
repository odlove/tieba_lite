package app.tiebalite.feature.thread.common.post

import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.request.transitionFactory
import coil3.transition.CrossfadeTransition
import coil3.transition.Transition
import coil3.transition.TransitionTarget

@Composable
internal fun ThreadPostContentSection(
    body: ThreadPostBody,
    modifier: Modifier = Modifier,
) {
    val blocks =
        remember(body) {
            buildThreadPostContentBlocks(
                body = body,
            )
        }
    if (blocks.isEmpty()) {
        return
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is ThreadPostContentBlock.Text -> {
                    ThreadPostRichText(
                        inline = block.inline,
                    )
                }

                is ThreadPostContentBlock.ImageGroup -> {
                    ThreadPostImageGrid(images = block.images)
                }

                is ThreadPostContentBlock.MediaHint -> {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun buildThreadPostContentBlocks(
    body: ThreadPostBody,
): List<ThreadPostContentBlock> {
    val blocks = mutableListOf<ThreadPostContentBlock>()

    if (body.inline.isNotEmpty()) {
        blocks += ThreadPostContentBlock.Text(inline = body.inline)
    }

    val images =
        body.media
            .asSequence()
            .mapNotNull { part -> part as? ThreadPostBody.MediaPart.Image }
            .filter { image -> image.url.isNotBlank() }
            .distinctBy { image -> image.url }
            .toList()
    if (images.isNotEmpty()) {
        blocks += ThreadPostContentBlock.ImageGroup(images = images)
    }

    body.media.forEach { part ->
        when (part) {
            is ThreadPostBody.MediaPart.Image -> Unit
            is ThreadPostBody.MediaPart.Video -> {
                blocks += ThreadPostContentBlock.MediaHint(text = "视频")
            }

            is ThreadPostBody.MediaPart.Voice -> {
                blocks +=
                    ThreadPostContentBlock.MediaHint(
                        text = if (part.durationSeconds > 0) "语音 ${part.durationSeconds}s" else "语音",
                    )
            }
        }
    }

    return blocks
}

@Composable
private fun ThreadPostImageGrid(images: List<ThreadPostBody.MediaPart.Image>) {
    if (images.isEmpty()) {
        return
    }
    val context = LocalContext.current
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val containerWidthDp = with(LocalDensity.current) { containerWidthPx.toDp() }
    val widthFraction = if (containerWidthDp < 600.dp) 1f else 0.5f
    Column(
        modifier = Modifier.fillMaxWidth(widthFraction),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        images.forEach { image ->
            val requestBuilder =
                ImageRequest
                    .Builder(context)
                    .data(image.url)
                    .transitionFactory(AlwaysCrossfadeTransitionFactory)
            if (isDebuggable) {
                requestBuilder.listener(
                    onSuccess = { request, result ->
                        Log.d(
                            ThreadImageDebugTag,
                            "post success source=${result.dataSource} data=${request.data}",
                        )
                    },
                    onError = { request, result ->
                        Log.d(
                            ThreadImageDebugTag,
                            "post error data=${request.data} throwable=${result.throwable}",
                        )
                    },
                )
            }
            AsyncImage(
                model = requestBuilder.build(),
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

private sealed interface ThreadPostContentBlock {
    data class Text(
        val inline: List<ThreadPostBody.InlinePart>,
    ) : ThreadPostContentBlock

    data class ImageGroup(
        val images: List<ThreadPostBody.MediaPart.Image>,
    ) : ThreadPostContentBlock

    data class MediaHint(
        val text: String,
    ) : ThreadPostContentBlock
}

private const val ThreadImageDebugTag = "ThreadImageDebug"

private val AlwaysCrossfadeTransitionFactory =
    object : Transition.Factory {
        override fun create(
            target: TransitionTarget,
            result: ImageResult,
        ): Transition {
            if (result !is SuccessResult) {
                return Transition.Factory.NONE.create(target, result)
            }
            return CrossfadeTransition(
                target = target,
                result = result,
                durationMillis = 200,
                preferExactIntrinsicSize = false,
            )
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
