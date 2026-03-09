package app.tiebalite.feature.imageviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import kotlinx.coroutines.launch

@Composable
internal fun ImageViewerScreen(
    paddingValues: PaddingValues,
    args: ImageViewerArgs,
    onBack: () -> Unit,
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
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
                .background(Color.Black),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            CoilZoomAsyncImage(
                model = args.items[page].imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                zoomState = rememberCoilZoomState(),
                onTap = {
                    onBack()
                },
            )
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                ),
                        ),
                    )
                    .navigationBarsPadding()
                    .padding(
                        start = paddingValues.calculateStartPadding(layoutDirection) + 16.dp,
                        end = paddingValues.calculateEndPadding(layoutDirection) + 16.dp,
                        top = 28.dp,
                        bottom = 12.dp,
                    ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${args.items.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    modifier = Modifier.size(80.dp),
                    onClick = {
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
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "下载图片",
                        tint = Color.White,
                    )
                    }
            }
        }
    }
}
