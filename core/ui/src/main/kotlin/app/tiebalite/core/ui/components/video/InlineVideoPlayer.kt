package app.tiebalite.core.ui.components.video

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun InlineVideoPlayer(
    player: Player,
    modifier: Modifier = Modifier,
    useController: Boolean = false,
    onVisibilityChanged: (Boolean) -> Unit = {},
    onViewReleased: () -> Unit = {},
) {
    var lastVisible by remember { mutableStateOf<Boolean?>(null) }
    val currentOnVisibilityChanged by rememberUpdatedState(onVisibilityChanged)
    val currentOnViewReleased by rememberUpdatedState(onViewReleased)

    AndroidView(
        modifier =
            modifier.onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                val rootBounds = coordinates.findRootCoordinates().boundsInWindow()
                val isVisible = bounds.overlaps(rootBounds)
                if (lastVisible != isVisible) {
                    lastVisible = isVisible
                    currentOnVisibilityChanged(isVisible)
                }
            },
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                this.useController = useController
                this.player = player
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        onRelease = { playerView ->
            currentOnViewReleased()
            playerView.player = null
        },
    )
}
