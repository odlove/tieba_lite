package app.tiebalite.feature.thread

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.feature.thread.R
import app.tiebalite.feature.thread.main.state.ThreadUiEvent
import app.tiebalite.feature.thread.main.state.ThreadViewModel
import app.tiebalite.feature.thread.main.screen.ThreadScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ThreadRoute(
    paddingValues: PaddingValues,
    threadId: Long,
    onBack: () -> Unit,
    onOpenForum: (String) -> Unit,
    onOpenSubPosts: (postId: Long) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
    viewModel: ThreadViewModel = viewModel(factory = ThreadViewModel.factory(threadId)),
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    val uiState by viewModel.uiState.collectAsState()
    val copiedMessage = stringResource(R.string.thread_link_copied)

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is ThreadUiEvent.CopyThreadLink -> {
                    val threadUrl = "https://tieba.baidu.com/p/${event.threadId}"
                    clipboard.setClipEntry(
                        ClipData
                            .newPlainText("thread_link", threadUrl)
                            .toClipEntry(),
                    )
                    Toast.makeText(currentContext, copiedMessage, Toast.LENGTH_SHORT).show()
                }
                is ThreadUiEvent.ShowToast -> {
                    Toast.makeText(currentContext, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ThreadScreen(
        paddingValues = paddingValues,
        state = uiState,
        onBack = onBack,
        onOpenForum = onOpenForum,
        onCopyThreadLink = viewModel::copyThreadLink,
        onSetSeeLz = viewModel::setSeeLz,
        onSetSortType = viewModel::setSortType,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::refresh,
        onOpenSubPosts = onOpenSubPosts,
        onOpenImageViewer = onOpenImageViewer,
    )
}
