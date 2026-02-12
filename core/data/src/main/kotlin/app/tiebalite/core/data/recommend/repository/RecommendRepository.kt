package app.tiebalite.core.data.recommend.repository

import app.tiebalite.core.model.recommend.RecommendItem

interface RecommendRepository {
    suspend fun loadFeed(): Result<List<RecommendItem>>
}
