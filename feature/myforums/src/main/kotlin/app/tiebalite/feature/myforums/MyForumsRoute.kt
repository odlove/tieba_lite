package app.tiebalite.feature.myforums

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MyForumsRoute(
    paddingValues: PaddingValues,
    onOpenForum: (String) -> Unit,
    viewModel: MyForumsViewModel = viewModel(factory = MyForumsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    MyForumsScreen(
        paddingValues = paddingValues,
        state = uiState,
        onRetry = viewModel::refresh,
        onOpenForum = onOpenForum,
    )
}
