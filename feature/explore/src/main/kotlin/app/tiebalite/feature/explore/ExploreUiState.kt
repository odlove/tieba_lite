package app.tiebalite.feature.explore

import app.tiebalite.core.model.recommend.RecommendItem

sealed interface ExploreUiState {
    data object Loading : ExploreUiState

    data object Empty : ExploreUiState

    data class Success(
        val items: List<RecommendItem>,
    ) : ExploreUiState

    data class Error(
        val message: String,
    ) : ExploreUiState
}
