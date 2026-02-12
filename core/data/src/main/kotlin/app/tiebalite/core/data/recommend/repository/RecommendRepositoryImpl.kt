package app.tiebalite.core.data.recommend.repository

import app.tiebalite.core.data.recommend.mapper.PersonalizedFeedMapper
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.data.recommend.remote.RecommendRemoteDataSource

class RecommendRepositoryImpl(
    private val remoteDataSource: RecommendRemoteDataSource,
    private val mapper: PersonalizedFeedMapper = PersonalizedFeedMapper(),
) : RecommendRepository {
    override suspend fun loadFeed(
        loadType: RecommendLoadType,
        page: Int,
    ): Result<List<RecommendItem>> =
        remoteDataSource.loadFeed(loadType = loadType, page = page).mapCatching(mapper::map)
}
