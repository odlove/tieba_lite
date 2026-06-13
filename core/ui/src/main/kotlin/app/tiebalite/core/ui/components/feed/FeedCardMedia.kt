package app.tiebalite.core.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.model.recommend.RecommendVideo
import app.tiebalite.core.ui.components.video.InlineVideoControlLayer
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
internal fun FeedCardMedia(
    item: RecommendItem,
    isVideoPlaying: Boolean = false,
    hasRenderedFirstFrame: Boolean = false,
    videoPlayer: Player? = null,
    videoPlayerContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onPlayVideo: (() -> Unit)? = null,
) {
    val video = item.video
    if (video != null) {
        FeedCardVideoMedia(
            video = video,
            isPlaying = isVideoPlaying,
            hasRenderedFirstFrame = hasRenderedFirstFrame,
            player = videoPlayer,
            videoPlayerContent = videoPlayerContent,
            onPlayVideo = onPlayVideo,
        )
        return
    }

    val cover = item.coverImageUrl?.trim().orEmpty()
    val imageCount = item.images.size
    if (cover.isBlank()) {
        return
    }
    FeedCardMediaFrame(aspectRatio = FeedImageAspectRatio) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(cover)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(FeedImageAspectRatio),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .optionalClickable(onClick = onClick)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f)),
        ) {
            if (imageCount > 1) {
                ImageCountBadge(
                    imageCount = imageCount,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun FeedCardVideoMedia(
    video: RecommendVideo,
    isPlaying: Boolean,
    hasRenderedFirstFrame: Boolean,
    player: Player?,
    videoPlayerContent: (@Composable () -> Unit)?,
    onPlayVideo: (() -> Unit)?,
) {
    val aspectRatio = video.aspectRatioOrDefault()
    FeedCardMediaFrame(aspectRatio = aspectRatio) {
        if (isPlaying && videoPlayerContent != null) {
            Box(modifier = Modifier.matchParentSize()) {
                videoPlayerContent()
            }
        }
        if (isPlaying && hasRenderedFirstFrame) {
            if (player != null) {
                InlineVideoControlLayer(
                    player = player,
                    videoUrl = video.url,
                    modifier = Modifier.matchParentSize(),
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                )
            }
        }
        if (!isPlaying || !hasRenderedFirstFrame) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(video.coverUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(aspectRatio),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .optionalClickable(
                            onClick = onPlayVideo,
                            onClickLabel = "播放视频",
                            role = Role.Button,
                        )
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f)),
            ) {
                if (!isPlaying && onPlayVideo != null) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
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
                video.durationSeconds?.let { durationSeconds ->
                    DurationBadge(
                        durationSeconds = durationSeconds,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedCardMediaFrame(
    aspectRatio: Float,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(FeedMediaWidthFraction)
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            content = content,
        )
    }
}

@Composable
private fun ImageCountBadge(
    imageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(100))
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.58f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color.White,
        )
        Text(
            text = imageCount.toString(),
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun DurationBadge(
    durationSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = durationSeconds.formatDuration(),
        modifier =
            modifier
                .clip(RoundedCornerShape(100))
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.58f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        color = Color.White,
        fontSize = 12.sp,
    )
}

private fun RecommendVideo.aspectRatioOrDefault(): Float {
    val videoWidth = width
    val videoHeight = height
    if (videoWidth == null || videoHeight == null || videoWidth <= 0 || videoHeight <= 0) {
        return FeedImageAspectRatio
    }
    return (videoWidth.toFloat() / videoHeight).coerceIn(MinVideoAspectRatio, FeedImageAspectRatio)
}

private fun Int.formatDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private const val MinVideoAspectRatio = 0.75f
private const val FeedImageAspectRatio = 1.85f
private const val FeedMediaWidthFraction = 0.85f

private fun Modifier.optionalClickable(
    onClick: (() -> Unit)?,
    onClickLabel: String? = null,
    role: Role? = null,
): Modifier =
    if (onClick == null) {
        this
    } else {
        clickable(
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick,
        )
    }
