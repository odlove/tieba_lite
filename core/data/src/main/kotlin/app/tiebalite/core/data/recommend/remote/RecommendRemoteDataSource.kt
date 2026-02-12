package app.tiebalite.core.data.recommend.remote

import app.tiebalite.core.data.recommend.repository.RecommendLoadType
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedNetworkSource

class RecommendRemoteDataSource(
    private val personalizedNetworkSource: PersonalizedNetworkSource,
) {
    suspend fun loadFeed(
        loadType: RecommendLoadType,
        page: Int,
    ): Result<PersonalizedFeedRaw> =
        personalizedNetworkSource.fetchFeed(loadType = loadType.wireValue, page = page)
}
