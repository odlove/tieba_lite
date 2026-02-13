package app.tiebalite.core.data.recommend.remote

import app.tiebalite.core.data.recommend.repository.RecommendLoadType
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedNetworkSource

class RecommendRemoteDataSource(
    private val personalizedNetworkSource: PersonalizedNetworkSource,
    private val sessionProvider: () -> AuthSession? = { null },
    private val tbsProvider: () -> String? = { null },
) {
    suspend fun loadFeed(
        loadType: RecommendLoadType,
        page: Int,
    ): Result<PersonalizedFeedRaw> {
        val session = sessionProvider()
        return personalizedNetworkSource.fetchFeed(
            loadType = loadType.wireValue,
            page = page,
            bduss = session?.bduss,
            stoken = session?.stoken,
            tbs = tbsProvider(),
        )
    }
}
