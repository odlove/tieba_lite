package app.tiebalite.core.data.recommend.remote

import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedNetworkSource

class RecommendRemoteDataSource(
    private val personalizedNetworkSource: PersonalizedNetworkSource,
) {
    suspend fun loadFeed(): Result<PersonalizedFeedRaw> =
        personalizedNetworkSource.fetchFeed()
}
