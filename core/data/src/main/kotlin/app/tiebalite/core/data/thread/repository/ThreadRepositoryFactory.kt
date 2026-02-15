package app.tiebalite.core.data.thread.repository

import app.tiebalite.core.data.thread.remote.ThreadRemoteDataSource
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.source.tbclient.thread.TbClientThreadNetwork

object ThreadRepositoryFactory {
    fun create(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        sessionProvider: () -> AuthSession? = { null },
        tbsProvider: () -> String? = { null },
    ): ThreadRepository {
        val pbPageNetworkSource = TbClientThreadNetwork.createPbPageNetworkSource(baseUrl = baseUrl)
        val remoteDataSource =
            ThreadRemoteDataSource(
                pbPageNetworkSource = pbPageNetworkSource,
                sessionProvider = sessionProvider,
                tbsProvider = tbsProvider,
            )
        return ThreadRepositoryImpl(
            remoteDataSource = remoteDataSource,
        )
    }
}
