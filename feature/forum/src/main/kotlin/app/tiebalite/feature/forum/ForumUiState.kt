package app.tiebalite.feature.forum

import app.tiebalite.core.model.forum.ForumHeader
import app.tiebalite.core.model.recommend.RecommendItem

data class ForumUiState(
    val header: ForumHeader? = null,
    val items: List<RecommendItem> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
)
