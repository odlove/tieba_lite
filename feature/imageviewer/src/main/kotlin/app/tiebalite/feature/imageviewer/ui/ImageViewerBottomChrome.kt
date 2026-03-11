package app.tiebalite.feature.imageviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
internal fun ImageViewerBottomScrim(
    paddingValues: PaddingValues,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer(alpha = alpha)
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                            ),
                    ),
                )
                .navigationBarsPadding()
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    top = ImageViewerBottomScrimTopPadding,
                ),
    )
}

@Composable
internal fun ImageViewerBottomBar(
    currentPage: Int,
    totalCount: Int,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${currentPage + 1} / $totalCount",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier.size(80.dp),
            onClick = onDownload,
        ) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = "下载图片",
                tint = Color.White,
            )
        }
    }
}

@Composable
internal fun Modifier.viewerBottomChromePadding(paddingValues: PaddingValues): Modifier {
    val layoutDirection = LocalLayoutDirection.current
    return this
        .navigationBarsPadding()
        .padding(
            start = paddingValues.calculateStartPadding(layoutDirection) + ImageViewerBottomContentHorizontalPadding,
            end = paddingValues.calculateEndPadding(layoutDirection) + ImageViewerBottomContentHorizontalPadding,
            bottom = ImageViewerBottomContentBottomPadding,
        )
}

// Controls how far upward the bottom gradient extends.
private val ImageViewerBottomScrimTopPadding = 80.dp

// Controls the horizontal inset for the page indicator and download button.
private val ImageViewerBottomContentHorizontalPadding = 16.dp

// Controls how close the page indicator and download button sit to the bottom safe area.
private val ImageViewerBottomContentBottomPadding = 4.dp
