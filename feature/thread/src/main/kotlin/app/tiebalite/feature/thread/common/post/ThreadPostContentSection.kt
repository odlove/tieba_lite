package app.tiebalite.feature.thread.common.post

import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.core.model.imageviewer.ImageViewerItem
import app.tiebalite.core.model.text.RichTextPart
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
    onOpenImageViewer: ((ImageViewerArgs) -> Unit)? = null,
    playingVideoKey: String? = null,
    videoKeyForVideo: ((Int, ThreadPostBody.MediaPart.Video) -> String)? = null,
    videoPlayerContent: (@Composable (String) -> Unit)? = null,
    onPlayVideo: ((String, String) -> Unit)? = null,
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
                    ThreadPostImageGrid(
                        images = block.images,
                        onOpenImageViewer = onOpenImageViewer,
                    )
                }

                is ThreadPostContentBlock.Video -> {
                    val videoKey = videoKeyForVideo?.invoke(block.index, block.video)
                    if (videoKey != null && videoPlayerContent != null && onPlayVideo != null) {
                        ThreadPostVideoBlock(
                            video = block.video,
                            isPlaying = playingVideoKey == videoKey,
                            videoPlayerContent = { videoPlayerContent(videoKey) },
                            onPlayVideo = { videoUrl ->
                                onPlayVideo(videoKey, videoUrl)
                            },
                        )
                    } else {
                        ThreadPostMediaHint(text = "视频")
                    }
                }

                is ThreadPostContentBlock.MediaHint -> {
                    ThreadPostMediaHint(text = block.text)
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

    var videoIndex = 0
    body.media.forEach { part ->
        when (part) {
            is ThreadPostBody.MediaPart.Image -> Unit
            is ThreadPostBody.MediaPart.Video -> {
                blocks += ThreadPostContentBlock.Video(index = videoIndex, video = part)
                videoIndex += 1
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
private fun ThreadPostVideoBlock(
    video: ThreadPostBody.MediaPart.Video,
    isPlaying: Boolean,
    videoPlayerContent: @Composable () -> Unit,
    onPlayVideo: (String) -> Unit,
) {
    val videoUrl = video.videoUrl?.takeIf { url -> url.isNotBlank() }
    val coverUrl = video.coverUrl?.takeIf { url -> url.isNotBlank() }
    if (videoUrl == null) {
        ThreadPostMediaHint(text = "视频")
        return
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(ThreadVideoAspectRatio)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (isPlaying) {
            videoPlayerContent()
        } else {
            if (coverUrl != null) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(coverUrl)
                            .transitionFactory(AlwaysCrossfadeTransitionFactory)
                            .build(),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickable(
                            onClickLabel = "播放视频",
                            role = Role.Button,
                        ) {
                            onPlayVideo(videoUrl)
                        }
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(100))
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.58f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreadPostMediaHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ThreadPostImageGrid(
    images: List<ThreadPostBody.MediaPart.Image>,
    onOpenImageViewer: ((ImageViewerArgs) -> Unit)? = null,
) {
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
        images.forEachIndexed { index, image ->
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
                        .clickable(enabled = onOpenImageViewer != null) {
                            val imageViewerArgs = images.toImageViewerArgs(initialIndex = index) ?: return@clickable
                            onOpenImageViewer?.invoke(imageViewerArgs)
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private sealed interface ThreadPostContentBlock {
    data class Text(
        val inline: List<RichTextPart>,
    ) : ThreadPostContentBlock

    data class ImageGroup(
        val images: List<ThreadPostBody.MediaPart.Image>,
    ) : ThreadPostContentBlock

    data class Video(
        val index: Int,
        val video: ThreadPostBody.MediaPart.Video,
    ) : ThreadPostContentBlock

    data class MediaHint(
        val text: String,
    ) : ThreadPostContentBlock
}

private const val ThreadImageDebugTag = "ThreadImageDebug"
private const val ThreadVideoAspectRatio = 16f / 9f

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

private fun List<ThreadPostBody.MediaPart.Image>.toImageViewerArgs(initialIndex: Int): ImageViewerArgs? {
    if (isEmpty() || initialIndex !in indices) {
        return null
    }
    return ImageViewerArgs(
        items =
            mapIndexed { index, image ->
                ImageViewerItem(
                    id = image.url.ifBlank { "thread-image-$index" },
                    imageUrl = image.url,
                    width = image.width,
                    height = image.height,
                )
            },
        initialIndex = initialIndex,
    )
}

private fun ThreadPostBody.MediaPart.Image.aspectRatioOrDefault(defaultRatio: Float = 1f): Float {
    val imageWidth = width ?: return defaultRatio
    val imageHeight = height ?: return defaultRatio
    if (imageWidth <= 0 || imageHeight <= 0) {
        return defaultRatio
    }
    return imageWidth.toFloat() / imageHeight.toFloat()
}
