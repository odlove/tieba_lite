package app.tiebalite.core.data.forum.repository

import app.tiebalite.core.data.forum.remote.ForumRemoteDataSource
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.source.tbclient.forum.TbClientForumNetwork

object ForumRepositoryFactory {
    fun create(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        sessionProvider: () -> AuthSession? = { null },
    ): ForumRepository {
        val frsPageNetworkSource = TbClientForumNetwork.createFrsPageNetworkSource(baseUrl = baseUrl)
        return ForumRepositoryImpl(
            remoteDataSource =
                ForumRemoteDataSource(
                    frsPageNetworkSource = frsPageNetworkSource,
                    sessionProvider = sessionProvider,
                ),
        )
    }
}
