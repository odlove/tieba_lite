package app.tiebalite.core.data.recommend.repository

import app.tiebalite.core.data.recommend.model.RecommendItem

interface RecommendRepository {
    suspend fun loadFeed(): Result<List<RecommendItem>>
}
