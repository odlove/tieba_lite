package app.tiebalite.feature.thread

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tiebalite.feature.thread.subposts.state.ThreadSubPostsViewModel
import app.tiebalite.feature.thread.subposts.screen.ThreadSubPostsScreen

@Composable
fun ThreadSubPostsRoute(
    paddingValues: PaddingValues,
    threadId: Long,
    postId: Long,
    onBack: () -> Unit,
    viewModel: ThreadSubPostsViewModel =
        viewModel(factory = ThreadSubPostsViewModel.factory(threadId = threadId, postId = postId)),
) {
    val uiState by viewModel.uiState.collectAsState()
    ThreadSubPostsScreen(
        paddingValues = paddingValues,
        state = uiState,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
    )
}
