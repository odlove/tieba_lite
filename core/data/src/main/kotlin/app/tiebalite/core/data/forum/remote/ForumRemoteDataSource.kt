package app.tiebalite.core.data.forum.remote

import app.tiebalite.core.model.auth.AuthAccount
import app.tiebalite.core.model.error.UserVisibleException
import app.tiebalite.core.network.source.tbclient.forum.ForumLikeNetworkSource
import app.tiebalite.core.network.source.tbclient.forum.FrsPageNetworkSource
import app.tiebalite.core.network.source.tbclient.forum.FrsPageRaw

internal class ForumRemoteDataSource(
    private val frsPageClient: ForumPageRemoteClient,
    private val forumLikeClient: ForumLikeRemoteClient,
    private val accountProvider: () -> AuthAccount? = { null },
) {
    suspend fun loadForumPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int = DEFAULT_SORT_TYPE,
    ): Result<FrsPageRaw> {
        val session = accountProvider()?.session
        return frsPageClient.fetchPage(
            forumName = forumName,
            page = page,
            loadType = loadType,
            sortType = sortType,
            bduss = session?.bduss,
            stoken = session?.stoken,
        )
    }

    suspend fun followForum(
        forumId: Long,
        forumName: String,
    ): Result<Unit> {
        val account = accountProvider()
            ?: return Result.failure(UserVisibleException("请先登录"))
        val profile = account.profile
            ?: return Result.failure(UserVisibleException("账号资料不完整，请刷新账号信息"))
        val tbs = account.session.tbs
            ?: return Result.failure(UserVisibleException("登录状态不完整，请重新登录"))
        return forumLikeClient.followForum(
            bduss = account.session.bduss,
            tbs = tbs,
            forumName = forumName,
            forumId = forumId,
            userId = profile.userId,
            userName = profile.userName,
        )
    }

    suspend fun unfollowForum(
        forumId: Long,
        forumName: String,
    ): Result<Unit> {
        val account = accountProvider()
            ?: return Result.failure(UserVisibleException("请先登录"))
        val tbs = account.session.tbs
            ?: return Result.failure(UserVisibleException("登录状态不完整，请重新登录"))
        return forumLikeClient.unfollowForum(
            bduss = account.session.bduss,
            tbs = tbs,
            forumName = forumName,
            forumId = forumId,
        )
    }
}

internal interface ForumPageRemoteClient {
    suspend fun fetchPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        bduss: String?,
        stoken: String?,
    ): Result<FrsPageRaw>
}

internal interface ForumLikeRemoteClient {
    suspend fun followForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
        userId: String,
        userName: String,
    ): Result<Unit>

    suspend fun unfollowForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
    ): Result<Unit>
}

internal class NetworkForumPageRemoteClient(
    private val source: FrsPageNetworkSource,
) : ForumPageRemoteClient {
    override suspend fun fetchPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        bduss: String?,
        stoken: String?,
    ): Result<FrsPageRaw> =
        source.fetchPage(
            forumName = forumName,
            page = page,
            loadType = loadType,
            sortType = sortType,
            bduss = bduss,
            stoken = stoken,
        )
}

internal class NetworkForumLikeRemoteClient(
    private val source: ForumLikeNetworkSource,
) : ForumLikeRemoteClient {
    override suspend fun followForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
        userId: String,
        userName: String,
    ): Result<Unit> =
        source.followForum(
            bduss = bduss,
            tbs = tbs,
            forumName = forumName,
            forumId = forumId,
            userId = userId,
            userName = userName,
        )

    override suspend fun unfollowForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
    ): Result<Unit> =
        source.unfollowForum(
            bduss = bduss,
            tbs = tbs,
            forumName = forumName,
            forumId = forumId,
        )
}

private const val DEFAULT_SORT_TYPE = -1
