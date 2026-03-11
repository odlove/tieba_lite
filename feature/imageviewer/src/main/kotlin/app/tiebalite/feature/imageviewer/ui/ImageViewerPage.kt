package app.tiebalite.feature.imageviewer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalViewConfiguration
import app.tiebalite.feature.imageviewer.gesture.ImageViewerDragDismissState
import app.tiebalite.feature.imageviewer.gesture.imageViewerDragDismiss
import app.tiebalite.feature.imageviewer.gesture.isAtRestTransform
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState

@Composable
internal fun ImageViewerPage(
    imageUrl: String,
    isCurrentPage: Boolean,
    dragDismissState: ImageViewerDragDismissState,
    onTap: () -> Unit,
    onDragDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoomState = rememberCoilZoomState()
    val viewConfiguration = LocalViewConfiguration.current
    val canDismissByDrag =
        isCurrentPage &&
            zoomState.zoomable.userTransform.isAtRestTransform()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (isCurrentPage) {
                        translationY = dragDismissState.offsetY
                        scaleX = dragDismissState.viewerScale
                        scaleY = dragDismissState.viewerScale
                        alpha = dragDismissState.contentAlpha
                    }
                }
                .imageViewerDragDismiss(
                    enabled = canDismissByDrag,
                    state = dragDismissState,
                    viewConfiguration = viewConfiguration,
                    onDismissed = onDragDismissed,
                ),
    ) {
        CoilZoomAsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            zoomState = zoomState,
            onTap = { onTap() },
        )
    }
}
