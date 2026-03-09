package app.tiebalite.feature.thread

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.feature.thread.main.state.ThreadUiEvent
import app.tiebalite.feature.thread.main.state.ThreadViewModel
import app.tiebalite.feature.thread.main.screen.ThreadScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ThreadRoute(
    paddingValues: PaddingValues,
    threadId: Long,
    onBack: () -> Unit,
    onOpenSubPosts: (postId: Long) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
    viewModel: ThreadViewModel = viewModel(factory = ThreadViewModel.factory(threadId)),
) {
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
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
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::refresh,
        onOpenSubPosts = onOpenSubPosts,
        onOpenImageViewer = onOpenImageViewer,
    )
}
