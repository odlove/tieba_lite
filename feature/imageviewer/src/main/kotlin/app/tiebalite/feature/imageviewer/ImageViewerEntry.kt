package app.tiebalite.feature.imageviewer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.feature.imageviewer.ui.ImageViewerScreen
import app.tiebalite.feature.imageviewer.ui.ImageViewerStatusBarEffect

@Composable
fun ImageViewerEntry(
    paddingValues: PaddingValues,
    args: ImageViewerArgs,
    onBack: () -> Unit,
    onDragDismissed: () -> Unit,
) {
    ImageViewerStatusBarEffect()
    ImageViewerScreen(
        paddingValues = paddingValues,
        args = args,
        onBack = onBack,
        onDragDismissed = onDragDismissed,
    )
}
