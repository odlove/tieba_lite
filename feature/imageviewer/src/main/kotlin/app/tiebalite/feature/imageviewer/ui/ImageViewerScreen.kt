package app.tiebalite.feature.imageviewer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.feature.imageviewer.gesture.rememberImageViewerDragDismissState
import app.tiebalite.feature.imageviewer.save.ImageSaveResult
import app.tiebalite.feature.imageviewer.save.saveImageToGallery
import kotlinx.coroutines.launch

@Composable
internal fun ImageViewerScreen(
    paddingValues: PaddingValues,
    args: ImageViewerArgs,
    onBack: () -> Unit,
    onDragDismissed: () -> Unit,
) {
    if (args.items.isEmpty()) {
        LaunchedEffect(Unit) {
            onBack()
        }
        return
    }

    val initialPage = args.initialIndex.coerceIn(0, args.items.lastIndex)
    val pagerState =
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { args.items.size },
        )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dragDismissState = rememberImageViewerDragDismissState()
    var pendingDownloadUrl by rememberSaveable { mutableStateOf<String?>(null) }

    fun startSave(imageUrl: String) {
        scope.launch {
            val result = saveImageToGallery(context = context, imageUrl = imageUrl)
            when (result) {
                is ImageSaveResult.Success -> {
                    Toast.makeText(
                        context,
                        "已保存到 ${result.savedPath}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                is ImageSaveResult.Failure -> {
                    Toast.makeText(
                        context,
                        "保存失败",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    val requestStoragePermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            val imageUrl = pendingDownloadUrl
            pendingDownloadUrl = null
            if (granted && imageUrl != null) {
                startSave(imageUrl)
            } else if (!granted) {
                Toast.makeText(context, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show()
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dragDismissState.viewerAlpha)),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            userScrollEnabled = !dragDismissState.isDragging,
        ) { page ->
            ImageViewerPage(
                imageUrl = args.items[page].imageUrl,
                isCurrentPage = page == pagerState.currentPage,
                dragDismissState = dragDismissState,
                onTap = onBack,
                onDragDismissed = onDragDismissed,
            )
        }

        ImageViewerBottomScrim(
            modifier = Modifier.align(Alignment.BottomCenter),
            paddingValues = paddingValues,
            alpha = dragDismissState.contentAlpha,
        )

        ImageViewerBottomBar(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer(alpha = dragDismissState.contentAlpha)
                    .viewerBottomChromePadding(paddingValues),
            currentPage = pagerState.currentPage,
            totalCount = args.items.size,
            onDownload = {
                val imageUrl = args.items[pagerState.currentPage].imageUrl
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                    context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    startSave(imageUrl)
                } else {
                    pendingDownloadUrl = imageUrl
                    requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            },
        )
    }
}
