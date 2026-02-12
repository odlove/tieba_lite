package app.tiebalite.feature.explore

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ExploreRoute(
    paddingValues: PaddingValues,
    viewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    ExploreScreen(
        paddingValues = paddingValues,
        state = uiState,
        onRetry = viewModel::refresh,
    )
}
