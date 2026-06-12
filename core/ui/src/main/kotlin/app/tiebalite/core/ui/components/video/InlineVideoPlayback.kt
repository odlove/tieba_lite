package app.tiebalite.core.ui.components.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun rememberInlineVideoPlayback(): InlineVideoPlayback {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val playback =
        remember(context) {
            InlineVideoPlayback(ExoPlayer.Builder(context).build())
        }
    DisposableEffect(lifecycleOwner, playback) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> playback.pause()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    DisposableEffect(playback) {
        onDispose {
            playback.release()
        }
    }
    return playback
}

@Stable
class InlineVideoPlayback internal constructor(
    private val exoPlayer: ExoPlayer,
) {
    val player: Player = exoPlayer

    var playingItemId by mutableStateOf<String?>(null)
        private set

    private var playingVideoUrl: String? = null

    private var renderedFirstFrameItemId by mutableStateOf<String?>(null)

    private val listener =
        object : Player.Listener {
            override fun onRenderedFirstFrame() {
                renderedFirstFrameItemId = playingItemId
            }
        }

    init {
        exoPlayer.addListener(listener)
    }

    fun play(
        itemId: String,
        videoUrl: String,
    ) {
        if (playingItemId != itemId || playingVideoUrl != videoUrl) {
            exoPlayer.stop()
            renderedFirstFrameItemId = null
            playingItemId = itemId
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
            exoPlayer.prepare()
            playingVideoUrl = videoUrl
        }
        exoPlayer.playWhenReady = true
    }

    fun hasRenderedFirstFrame(itemId: String): Boolean = renderedFirstFrameItemId == itemId

    fun pause() {
        exoPlayer.pause()
    }

    fun pauseIfPlaying(itemId: String) {
        if (playingItemId == itemId) {
            exoPlayer.pause()
        }
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        playingItemId = null
        playingVideoUrl = null
        renderedFirstFrameItemId = null
    }

    internal fun release() {
        exoPlayer.removeListener(listener)
        renderedFirstFrameItemId = null
        exoPlayer.release()
    }
}
