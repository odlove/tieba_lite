package app.tiebalite.core.data.auth

import app.tiebalite.core.model.auth.AuthProfile
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.source.tbclient.auth.TbClientAuthNetwork
import app.tiebalite.core.network.source.tbclient.auth.TbClientLoginRaw
import app.tiebalite.core.network.source.tbclient.auth.TbClientLoginNetworkSource
import app.tiebalite.core.network.source.web.auth.WebAuthNetwork
import app.tiebalite.core.network.source.web.auth.WebMyInfoNetworkSource
import app.tiebalite.core.network.source.web.auth.WebMyInfoRaw

class AuthService(
    private val authStore: AuthStore,
    private val tbClientLoginNetworkSource: TbClientLoginNetworkSource = TbClientAuthNetwork.createLoginNetworkSource(),
    private val webMyInfoNetworkSource: WebMyInfoNetworkSource = WebAuthNetwork.createMyInfoNetworkSource(),
) {
    suspend fun loginWithWeb(
        session: AuthSession,
        rawCookie: String?,
    ): String {
        val accountId =
            authStore.upsertAccount(
                session = session,
                activate = false,
            )
        if (!rawCookie.isNullOrBlank()) {
            authStore.saveCookie(
                accountId = accountId,
                rawCookie = rawCookie,
            )
        }
        authStore.setActiveAccount(accountId)
        return accountId
    }

    suspend fun loginWithCredential(session: AuthSession): String {
        val accountId =
            authStore.upsertAccount(
                session = session,
                activate = false,
            )
        authStore.setActiveAccount(accountId)
        return accountId
    }

    suspend fun switchAccount(accountId: String): Boolean = authStore.setActiveAccount(accountId)

    suspend fun removeAccount(accountId: String): Boolean = authStore.removeAccount(accountId)

    suspend fun logoutActiveAccount(): Boolean = authStore.removeActiveAccount()

    suspend fun fetchProfileBySession(session: AuthSession): Result<AuthProfile> =
        tbClientLoginNetworkSource
            .login(
                bduss = session.bduss,
                stoken = session.stoken,
            ).map { raw ->
                raw.toAuthProfile()
            }

    suspend fun fetchProfileByActiveSession(): Result<AuthProfile> {
        val session = authStore.currentSession()
            ?: return Result.failure(IllegalStateException("no active session"))
        return fetchProfileBySession(session)
    }

    suspend fun fetchProfileByActiveCookie(): Result<AuthProfile> {
        val rawCookie = authStore.cookieOfActiveAccount()
            ?: return Result.failure(IllegalStateException("no active cookie"))
        return webMyInfoNetworkSource
            .fetchMyInfo(cookie = rawCookie)
            .map { raw ->
                raw.toAuthProfile()
            }
    }
}

private fun TbClientLoginRaw.toAuthProfile(): AuthProfile =
    AuthProfile(
        userId = userId,
        userName = userName,
        displayName = userName,
        avatarUrl = portrait,
        tbs = tbs,
    )

private fun WebMyInfoRaw.toAuthProfile(): AuthProfile =
    AuthProfile(
        userId = uid.takeIf { it > 0L }?.toString().orEmpty(),
        userName = name,
        displayName = showName.ifBlank { name },
        avatarUrl = avatarUrl,
        tbs = tbs,
    )
