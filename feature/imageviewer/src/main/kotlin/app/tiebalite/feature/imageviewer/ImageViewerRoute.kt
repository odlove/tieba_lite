package app.tiebalite.feature.imageviewer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import app.tiebalite.core.model.imageviewer.ImageViewerArgs

@Composable
fun ImageViewerRoute(
    paddingValues: PaddingValues,
    args: ImageViewerArgs,
    onBack: () -> Unit,
) {
    ImageViewerScreen(
        paddingValues = paddingValues,
        args = args,
        onBack = onBack,
    )
}
