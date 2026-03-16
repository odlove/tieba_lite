package app.tiebalite.core.data.myforums.remote

import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.source.tbclient.forum.ForumGuideNetworkSource
import app.tiebalite.core.network.source.tbclient.forum.ForumGuideRaw

class MyForumsRemoteDataSource(
    private val networkSource: ForumGuideNetworkSource,
    private val sessionProvider: () -> AuthSession?,
) {
    suspend fun loadForumGuide(): Result<ForumGuideRaw> {
        val session = sessionProvider()
            ?: return Result.failure(IllegalStateException("未登录"))
        return networkSource.fetchForumGuide(
            bduss = session.bduss,
            stoken = session.stoken,
        )
    }
}
