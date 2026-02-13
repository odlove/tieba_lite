package app.tiebalite.core.data.recommend.repository

import app.tiebalite.core.data.recommend.remote.RecommendRemoteDataSource
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.source.tbclient.recommend.TbClientRecommendNetwork

object RecommendRepositoryFactory {
    fun create(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        sessionProvider: () -> AuthSession? = { null },
        tbsProvider: () -> String? = { null },
    ): RecommendRepository {
        val personalizedNetworkSource =
            TbClientRecommendNetwork.createPersonalizedNetworkSource(baseUrl = baseUrl)
        val remoteDataSource =
            RecommendRemoteDataSource(
                personalizedNetworkSource = personalizedNetworkSource,
                sessionProvider = sessionProvider,
                tbsProvider = tbsProvider,
            )
        return RecommendRepositoryImpl(
            remoteDataSource = remoteDataSource,
        )
    }
}
