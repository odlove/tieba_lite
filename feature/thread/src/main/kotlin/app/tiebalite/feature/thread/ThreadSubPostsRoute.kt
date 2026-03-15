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
import app.tiebalite.feature.thread.subposts.state.ThreadSubPostsUiEvent
import app.tiebalite.feature.thread.subposts.state.ThreadSubPostsViewModel
import app.tiebalite.feature.thread.subposts.screen.ThreadSubPostsScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ThreadSubPostsRoute(
    paddingValues: PaddingValues,
    threadId: Long,
    postId: Long,
    onBack: () -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
    viewModel: ThreadSubPostsViewModel =
        viewModel(factory = ThreadSubPostsViewModel.factory(threadId = threadId, postId = postId)),
) {
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is ThreadSubPostsUiEvent.ShowToast -> {
                    Toast.makeText(currentContext, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ThreadSubPostsScreen(
        paddingValues = paddingValues,
        state = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
        onOpenImageViewer = onOpenImageViewer,
    )
}
