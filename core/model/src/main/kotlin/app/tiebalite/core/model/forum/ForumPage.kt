package app.tiebalite.core.model.forum

import app.tiebalite.core.model.recommend.RecommendItem

data class ForumPage(
    val header: ForumHeader,
    val items: List<RecommendItem> = emptyList(),
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
)
