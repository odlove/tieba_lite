package app.tiebalite.feature.forum

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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ForumRoute(
    paddingValues: PaddingValues,
    forumName: String,
    onBack: () -> Unit,
    onOpenThread: (String) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
    viewModel: ForumViewModel = viewModel(factory = ForumViewModel.factory(forumName)),
) {
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is ForumUiEvent.ShowToast -> {
                    Toast.makeText(currentContext, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ForumScreen(
        paddingValues = paddingValues,
        forumName = forumName,
        state = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::refresh,
        onOpenThread = onOpenThread,
        onOpenImageViewer = onOpenImageViewer,
    )
}
