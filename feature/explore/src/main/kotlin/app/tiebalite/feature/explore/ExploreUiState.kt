package app.tiebalite.feature.explore

import app.tiebalite.core.model.recommend.RecommendItem

data class ExploreUiState(
    val items: List<RecommendItem> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextPage: Int = 1,
    val errorMessage: String? = null,
)
