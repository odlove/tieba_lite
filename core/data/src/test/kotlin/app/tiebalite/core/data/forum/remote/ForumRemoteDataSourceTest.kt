package app.tiebalite.core.data.forum.remote

import app.tiebalite.core.model.auth.AuthAccount
import app.tiebalite.core.model.auth.AuthProfile
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.model.error.UserVisibleException
import app.tiebalite.core.network.source.tbclient.forum.FrsPageRaw
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForumRemoteDataSourceTest {
    @Test
    fun followForumForwardsAccountFields() {
        val forumLikeClient = FakeForumLikeRemoteClient()
        val dataSource =
            ForumRemoteDataSource(
                frsPageClient = FakeForumPageRemoteClient(),
                forumLikeClient = forumLikeClient,
                forumSignClient = FakeForumSignRemoteClient(),
                accountProvider = { account() },
            )

        val result =
            runBlocking {
                dataSource.followForum(
                    forumId = 155829,
                    forumName = "python",
                )
            }

        assertTrue(result.isSuccess)
        assertEquals("BDUSS", forumLikeClient.followCall?.bduss)
        assertEquals("TBS", forumLikeClient.followCall?.tbs)
        assertEquals("python", forumLikeClient.followCall?.forumName)
        assertEquals(155829L, forumLikeClient.followCall?.forumId)
        assertEquals("1", forumLikeClient.followCall?.userId)
        assertEquals("name", forumLikeClient.followCall?.userName)
    }

    @Test
    fun followForumFailsWithoutProfileAndDoesNotCallNetwork() {
        val forumLikeClient = FakeForumLikeRemoteClient()
        val dataSource =
            ForumRemoteDataSource(
                frsPageClient = FakeForumPageRemoteClient(),
                forumLikeClient = forumLikeClient,
                forumSignClient = FakeForumSignRemoteClient(),
                accountProvider = { account(profile = null) },
            )

        val result =
            runBlocking {
                dataSource.followForum(
                    forumId = 155829,
                    forumName = "python",
                )
            }

        assertTrue(result.isFailure)
        val throwable = result.exceptionOrNull() as UserVisibleException
        assertEquals("账号资料不完整，请刷新账号信息", throwable.userMessage)
        assertFalse(forumLikeClient.hasCalls)
    }

    @Test
    fun followForumFailsWithoutAccountAndDoesNotCallNetwork() {
        val forumLikeClient = FakeForumLikeRemoteClient()
        val dataSource =
            ForumRemoteDataSource(
                frsPageClient = FakeForumPageRemoteClient(),
                forumLikeClient = forumLikeClient,
                forumSignClient = FakeForumSignRemoteClient(),
                accountProvider = { null },
            )

        val result =
            runBlocking {
                dataSource.followForum(
                    forumId = 155829,
                    forumName = "python",
                )
            }

        assertTrue(result.isFailure)
        val throwable = result.exceptionOrNull() as UserVisibleException
        assertEquals("请先登录", throwable.userMessage)
        assertFalse(forumLikeClient.hasCalls)
    }

    @Test
    fun unfollowForumRequiresTbsAndDoesNotRequireProfile() {
        val forumLikeClient = FakeForumLikeRemoteClient()
        val dataSource =
            ForumRemoteDataSource(
                frsPageClient = FakeForumPageRemoteClient(),
                forumLikeClient = forumLikeClient,
                forumSignClient = FakeForumSignRemoteClient(),
                accountProvider = { account(profile = null) },
            )

        val result =
            runBlocking {
                dataSource.unfollowForum(
                    forumId = 155829,
                    forumName = "python",
                )
            }

        assertTrue(result.isSuccess)
        assertEquals("BDUSS", forumLikeClient.unfollowCall?.bduss)
        assertEquals("TBS", forumLikeClient.unfollowCall?.tbs)
        assertEquals("python", forumLikeClient.unfollowCall?.forumName)
        assertEquals(155829L, forumLikeClient.unfollowCall?.forumId)
    }

    @Test
    fun unfollowForumFailsWithoutTbsAndDoesNotCallNetwork() {
        val forumLikeClient = FakeForumLikeRemoteClient()
        val dataSource =
            ForumRemoteDataSource(
                frsPageClient = FakeForumPageRemoteClient(),
                forumLikeClient = forumLikeClient,
                forumSignClient = FakeForumSignRemoteClient(),
                accountProvider = { account(session = AuthSession(bduss = "BDUSS", stoken = "STOKEN")) },
            )

        val result =
            runBlocking {
                dataSource.unfollowForum(
                    forumId = 155829,
                    forumName = "python",
                )
            }

        assertTrue(result.isFailure)
        val throwable = result.exceptionOrNull() as UserVisibleException
        assertEquals("登录状态不完整，请重新登录", throwable.userMessage)
        assertFalse(forumLikeClient.hasCalls)
    }

    @Test
    fun signInForumForwardsAccountFieldsAndDoesNotRequireProfile() {
        val forumSignClient = FakeForumSignRemoteClient()
        val dataSource =
            ForumRemoteDataSource(
                frsPageClient = FakeForumPageRemoteClient(),
                forumLikeClient = FakeForumLikeRemoteClient(),
                forumSignClient = forumSignClient,
                accountProvider = { account(profile = null) },
            )

        val result =
            runBlocking {
                dataSource.signInForum(
                    forumId = 155829,
                    forumName = "python",
                )
            }

        assertTrue(result.isSuccess)
        assertEquals("BDUSS", forumSignClient.signCall?.bduss)
        assertEquals("TBS", forumSignClient.signCall?.tbs)
        assertEquals("python", forumSignClient.signCall?.forumName)
        assertEquals(155829L, forumSignClient.signCall?.forumId)
    }

    @Test
    fun signInForumFailsWithoutAccountAndDoesNotCallNetwork() {
        val forumSignClient = FakeForumSignRemoteClient()
        val dataSource =
            ForumRemoteDataSource(
                frsPageClient = FakeForumPageRemoteClient(),
                forumLikeClient = FakeForumLikeRemoteClient(),
                forumSignClient = forumSignClient,
                accountProvider = { null },
            )

        val result =
            runBlocking {
                dataSource.signInForum(
                    forumId = 155829,
                    forumName = "python",
                )
            }

        assertTrue(result.isFailure)
        val throwable = result.exceptionOrNull() as UserVisibleException
        assertEquals("请先登录", throwable.userMessage)
        assertFalse(forumSignClient.hasCalls)
    }

    @Test
    fun signInForumFailsWithoutTbsAndDoesNotCallNetwork() {
        val forumSignClient = FakeForumSignRemoteClient()
        val dataSource =
            ForumRemoteDataSource(
                frsPageClient = FakeForumPageRemoteClient(),
                forumLikeClient = FakeForumLikeRemoteClient(),
                forumSignClient = forumSignClient,
                accountProvider = { account(session = AuthSession(bduss = "BDUSS", stoken = "STOKEN")) },
            )

        val result =
            runBlocking {
                dataSource.signInForum(
                    forumId = 155829,
                    forumName = "python",
                )
            }

        assertTrue(result.isFailure)
        val throwable = result.exceptionOrNull() as UserVisibleException
        assertEquals("登录状态不完整，请重新登录", throwable.userMessage)
        assertFalse(forumSignClient.hasCalls)
    }

    private fun account(
        session: AuthSession = AuthSession(bduss = "BDUSS", stoken = "STOKEN", tbs = "TBS"),
        profile: AuthProfile? =
            AuthProfile(
                userId = "1",
                userName = "name",
                displayName = "Name",
                avatarUrl = "",
            ),
    ): AuthAccount =
        AuthAccount(
            accountId = "account",
            session = session,
            profile = profile,
            updatedAtMillis = 1L,
        )
}

private class FakeForumPageRemoteClient : ForumPageRemoteClient {
    override suspend fun fetchPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        bduss: String?,
        stoken: String?,
    ): Result<FrsPageRaw> = error("not used")
}

private class FakeForumLikeRemoteClient : ForumLikeRemoteClient {
    var followCall: FollowCall? = null
        private set
    var unfollowCall: UnfollowCall? = null
        private set

    val hasCalls: Boolean
        get() = followCall != null || unfollowCall != null

    override suspend fun followForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
        userId: String,
        userName: String,
    ): Result<Unit> {
        followCall =
            FollowCall(
                bduss = bduss,
                tbs = tbs,
                forumName = forumName,
                forumId = forumId,
                userId = userId,
                userName = userName,
            )
        return Result.success(Unit)
    }

    override suspend fun unfollowForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
    ): Result<Unit> {
        unfollowCall =
            UnfollowCall(
                bduss = bduss,
                tbs = tbs,
                forumName = forumName,
                forumId = forumId,
            )
        return Result.success(Unit)
    }
}

private data class FollowCall(
    val bduss: String,
    val tbs: String,
    val forumName: String,
    val forumId: Long,
    val userId: String,
    val userName: String,
)

private data class UnfollowCall(
    val bduss: String,
    val tbs: String,
    val forumName: String,
    val forumId: Long,
)

private class FakeForumSignRemoteClient : ForumSignRemoteClient {
    var signCall: SignCall? = null
        private set

    val hasCalls: Boolean
        get() = signCall != null

    override suspend fun signInForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
    ): Result<Unit> {
        signCall =
            SignCall(
                bduss = bduss,
                tbs = tbs,
                forumName = forumName,
                forumId = forumId,
            )
        return Result.success(Unit)
    }
}

private data class SignCall(
    val bduss: String,
    val tbs: String,
    val forumName: String,
    val forumId: Long,
)
