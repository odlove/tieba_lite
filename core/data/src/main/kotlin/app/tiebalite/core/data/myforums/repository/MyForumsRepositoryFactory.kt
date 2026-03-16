package app.tiebalite.core.data.myforums.repository

import app.tiebalite.core.data.myforums.remote.MyForumsRemoteDataSource
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.source.tbclient.forum.TbClientForumNetwork

object MyForumsRepositoryFactory {
    fun create(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        sessionProvider: () -> AuthSession?,
    ): MyForumsRepository {
        val networkSource = TbClientForumNetwork.createForumGuideNetworkSource(baseUrl = baseUrl)
        val remoteDataSource =
            MyForumsRemoteDataSource(
                networkSource = networkSource,
                sessionProvider = sessionProvider,
            )
        return MyForumsRepositoryImpl(remoteDataSource = remoteDataSource)
    }
}
