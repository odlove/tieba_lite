package app.tiebalite.core.ui.components.video

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.media3.ui.R as Media3UiR

@Composable
fun InlineVideoControlLayer(
    player: Player,
    videoUrl: String,
    modifier: Modifier = Modifier,
) {
    var controlsVisible by remember(player, videoUrl) { mutableStateOf(false) }
    var isInteracting by remember(player, videoUrl) { mutableStateOf(false) }
    var isPlaying by remember(player) { mutableStateOf(player.isPlaying) }
    var playbackState by remember(player) { mutableIntStateOf(player.playbackState) }

    DisposableEffect(player) {
        val listener =
            object : Player.Listener {
                override fun onIsPlayingChanged(value: Boolean) {
                    isPlaying = value
                }

                override fun onPlaybackStateChanged(value: Int) {
                    playbackState = value
                }
            }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    LaunchedEffect(isPlaying, playbackState) {
        if (!isPlaying || playbackState == Player.STATE_ENDED) {
            controlsVisible = true
        }
    }
    LaunchedEffect(controlsVisible, isPlaying, isInteracting, playbackState) {
        if (controlsVisible && isPlaying && !isInteracting && playbackState != Player.STATE_ENDED) {
            delay(ControlsAutoHideDelayMs)
            controlsVisible = false
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        controlsVisible = !controlsVisible
                    },
                ),
    ) {
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(animationSpec = tween(ControlsFadeInDurationMs)),
            exit = fadeOut(animationSpec = tween(ControlsFadeOutDurationMs)),
        ) {
            InlineVideoControls(
                player = player,
                videoUrl = videoUrl,
                onInteractionStart = {
                    isInteracting = true
                    controlsVisible = true
                },
                onInteractionEnd = {
                    isInteracting = false
                    controlsVisible = true
                },
            )
        }
    }
}

@Composable
fun InlineVideoControls(
    player: Player,
    videoUrl: String,
    modifier: Modifier = Modifier,
    onInteractionStart: () -> Unit = {},
    onInteractionEnd: () -> Unit = {},
) {
    var isPlaying by remember(player) { mutableStateOf(player.isPlaying) }
    var playWhenReady by remember(player) { mutableStateOf(player.playWhenReady) }
    var playbackState by remember(player) { mutableIntStateOf(player.playbackState) }
    var durationMs by remember(player) { mutableLongStateOf(player.duration.takeIf { it > 0 } ?: 0L) }
    var positionMs by remember(player) { mutableLongStateOf(player.currentPosition.coerceAtLeast(0L)) }
    var isSeeking by remember(player) { mutableStateOf(false) }
    var seekProgress by remember(player) { mutableFloatStateOf(0f) }

    DisposableEffect(player) {
        val listener =
            object : Player.Listener {
                override fun onIsPlayingChanged(value: Boolean) {
                    isPlaying = value
                }

                override fun onPlayWhenReadyChanged(
                    playWhenReadyValue: Boolean,
                    reason: Int,
                ) {
                    playWhenReady = playWhenReadyValue
                }

                override fun onPlaybackStateChanged(playbackStateValue: Int) {
                    playbackState = playbackStateValue
                    durationMs = player.duration.takeIf { it > 0 } ?: 0L
                    positionMs = player.currentPosition.coerceAtLeast(0L)
                }
            }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    LaunchedEffect(player) {
        while (true) {
            if (!isSeeking) {
                durationMs = player.duration.takeIf { it > 0 } ?: 0L
                positionMs = player.currentPosition.coerceAtLeast(0L)
            }
            delay(500)
        }
    }

    val progress =
        if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        } else {
            0f
        }
    val displayedProgress = if (isSeeking) seekProgress else progress
    val isPlaybackRequested = playWhenReady && playbackState != Player.STATE_ENDED

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        InlineVideoPlayPauseButton(
            isPlaying = isPlaybackRequested,
            onClick = {
                onInteractionStart()
                if (isPlaybackRequested) {
                    player.pause()
                } else {
                    if (player.playbackState == Player.STATE_ENDED) {
                        player.seekTo(0)
                    }
                    player.play()
                }
                onInteractionEnd()
            },
        )
        InlineVideoTimeBar(
            progress = displayedProgress,
            enabled = durationMs > 0L,
            onSeekStart = {
                isSeeking = true
                onInteractionStart()
            },
            onSeekChange = { value ->
                seekProgress = value
            },
            onSeekEnd = {
                if (durationMs > 0L) {
                    player.seekTo((seekProgress * durationMs).toLong())
                }
                isSeeking = false
                onInteractionEnd()
            },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(TimeBarTouchHeight),
        )
        InlineVideoMoreMenu(
            videoUrl = videoUrl,
            onInteractionStart = onInteractionStart,
            onInteractionEnd = onInteractionEnd,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
        )
    }
}

@Composable
private fun InlineVideoMoreMenu(
    videoUrl: String,
    onInteractionStart: () -> Unit,
    onInteractionEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember(videoUrl) { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = {
                onInteractionStart()
                expanded = true
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "更多视频操作",
                tint = Color.White,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onInteractionEnd()
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "复制直链",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                onClick = {
                    expanded = false
                    scope.launch {
                        try {
                            clipboard.setClipEntry(
                                ClipData
                                    .newPlainText("video_url", videoUrl)
                                    .toClipEntry(),
                            )
                            Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
                        } finally {
                            onInteractionEnd()
                        }
                    }
                },
                colors =
                    MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        }
    }
}

@Composable
private fun InlineVideoPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    val icon: Painter =
        painterResource(
            id =
                if (isPlaying) {
                    Media3UiR.drawable.exo_styled_controls_pause
                } else {
                    Media3UiR.drawable.exo_styled_controls_play
                },
        )
    Box(
        modifier =
            Modifier
                .size(64.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = icon,
            contentDescription = if (isPlaying) "暂停视频" else "播放视频",
            modifier = Modifier.size(52.dp),
        )
    }
}

@Composable
private fun InlineVideoTimeBar(
    progress: Float,
    enabled: Boolean,
    onSeekStart: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onSeekEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    fun progressAt(x: Float): Float {
        val width = size.width.toFloat()
        if (width <= 0f) {
            return 0f
        }
        return (x / width).coerceIn(0f, 1f)
    }

    Box(
        modifier =
            modifier
                .padding(horizontal = 8.dp)
                .onSizeChanged { size = it }
                .pointerInput(enabled, size) {
                    if (!enabled) {
                        return@pointerInput
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        onSeekStart()
                        onSeekChange(progressAt(down.position.x))
                        drag(down.id) { change ->
                            onSeekChange(progressAt(change.position.x))
                            change.consume()
                        }
                        onSeekEnd()
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            val barHeight = TimeBarHeight.toPx()
            val playedWidth = size.width * progress.coerceIn(0f, 1f)

            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(0f, centerY),
                end = Offset(size.width.toFloat(), centerY),
                strokeWidth = barHeight,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White,
                start = Offset(0f, centerY),
                end = Offset(playedWidth, centerY),
                strokeWidth = barHeight,
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = Color.White,
                radius = TimeBarScrubberRadius.toPx(),
                center = Offset(playedWidth, centerY),
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.28f),
                radius = TimeBarScrubberRadius.toPx(),
                center = Offset(playedWidth, centerY),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}

private val TimeBarTouchHeight = 32.dp
private val TimeBarHeight = 2.dp
private val TimeBarScrubberRadius = 5.dp
private const val ControlsAutoHideDelayMs = 3_000L
private const val ControlsFadeInDurationMs = 120
private const val ControlsFadeOutDurationMs = 160
