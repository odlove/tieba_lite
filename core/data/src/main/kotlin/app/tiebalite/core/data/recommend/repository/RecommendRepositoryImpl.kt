package app.tiebalite.core.data.recommend.repository

import app.tiebalite.core.data.recommend.mapper.PersonalizedFeedMapper
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.data.recommend.remote.RecommendRemoteDataSource

class RecommendRepositoryImpl(
    private val remoteDataSource: RecommendRemoteDataSource,
    private val mapper: PersonalizedFeedMapper = PersonalizedFeedMapper(),
) : RecommendRepository {
    override suspend fun loadFeed(): Result<List<RecommendItem>> =
        remoteDataSource.loadFeed().mapCatching(mapper::map)
}
