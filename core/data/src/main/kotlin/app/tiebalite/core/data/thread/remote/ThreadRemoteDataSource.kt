package app.tiebalite.core.data.thread.remote

import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.source.tbclient.thread.PbPageNetworkSource
import app.tiebalite.core.network.source.tbclient.thread.PbPageRaw

class ThreadRemoteDataSource(
    private val pbPageNetworkSource: PbPageNetworkSource,
    private val sessionProvider: () -> AuthSession? = { null },
    private val tbsProvider: () -> String? = { null },
) {
    suspend fun loadThreadPage(
        threadId: Long,
        page: Int,
    ): Result<PbPageRaw> {
        val session = sessionProvider()
        return pbPageNetworkSource.fetchPage(
            threadId = threadId,
            page = page,
            bduss = session?.bduss,
            stoken = session?.stoken,
            tbs = tbsProvider() ?: session?.tbs,
        )
    }
}
