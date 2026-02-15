package app.tiebalite.core.data.auth.service

import app.tiebalite.core.data.auth.store.AuthStore
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.source.tbclient.auth.TbClientAuthNetwork
import app.tiebalite.core.network.source.tbclient.auth.TbClientLoginNetworkSource
import app.tiebalite.core.network.source.web.auth.WebAuthNetwork
import app.tiebalite.core.network.source.web.auth.WebMyInfoNetworkSource

interface AuthService {
    suspend fun loginWithWeb(
        session: AuthSession,
        rawCookie: String?,
    ): Result<String>

    suspend fun loginWithCredential(session: AuthSession): Result<String>

    suspend fun switchAccount(accountId: String): Result<Unit>

    suspend fun removeAccount(accountId: String): Result<Unit>

    suspend fun logoutActive(): Result<Unit>

    suspend fun refreshActiveProfile(): Result<Unit>
}

internal class DefaultAuthService(
    private val authStore: AuthStore,
    private val tbClientLoginNetworkSource: TbClientLoginNetworkSource = TbClientAuthNetwork.createLoginNetworkSource(),
    private val webMyInfoNetworkSource: WebMyInfoNetworkSource = WebAuthNetwork.createMyInfoNetworkSource(),
) : AuthService {
    override suspend fun loginWithWeb(
        session: AuthSession,
        rawCookie: String?,
    ): Result<String> =
        runCatching {
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
            val activated = authStore.setActiveAccount(accountId)
            check(activated) { "failed to activate account" }
            refreshProfileForActive(preferCookie = !rawCookie.isNullOrBlank()).getOrNull()
            accountId
        }

    override suspend fun loginWithCredential(session: AuthSession): Result<String> =
        runCatching {
            val accountId =
                authStore.upsertAccount(
                    session = session,
                    activate = false,
                )
            val activated = authStore.setActiveAccount(accountId)
            check(activated) { "failed to activate account" }
            refreshProfileForActive(preferCookie = false).getOrNull()
            accountId
        }

    override suspend fun switchAccount(accountId: String): Result<Unit> =
        runCatching {
            val switched = authStore.setActiveAccount(accountId)
            check(switched) { "account not found" }
            refreshProfileForActive(preferCookie = authStore.cookieOf(accountId) != null).getOrNull()
        }

    override suspend fun removeAccount(accountId: String): Result<Unit> =
        runCatching {
            val removed = authStore.removeAccount(accountId)
            check(removed) { "account not found" }
        }

    override suspend fun logoutActive(): Result<Unit> =
        runCatching {
            authStore.setActiveAccount(null)
        }

    override suspend fun refreshActiveProfile(): Result<Unit> {
        val snapshot = authStore.state.value
        val activeAccountId = snapshot.activeAccountId
            ?: return Result.failure(IllegalStateException("no active account"))
        val preferCookie = authStore.cookieOf(activeAccountId) != null
        return refreshProfileForActive(preferCookie = preferCookie)
    }

    private suspend fun refreshProfileForActive(preferCookie: Boolean): Result<Unit> {
        val activeAccountId =
            authStore.state.value.activeAccountId
                ?: return Result.failure(IllegalStateException("no active account"))
        val result: Result<AuthProfilePayload> =
            if (preferCookie) {
                fetchProfilePayloadByActiveCookie().recoverCatching {
                    fetchProfilePayloadByActiveSession().getOrThrow()
                }
            } else {
                fetchProfilePayloadByActiveSession().recoverCatching {
                    fetchProfilePayloadByActiveCookie().getOrThrow()
                }
            }
        return result.mapCatching { payload ->
            val saved =
                authStore.saveProfile(
                    accountId = activeAccountId,
                    profile = payload.profile,
                    tbs = payload.tbs,
                )
            check(saved) { "active account missing while saving profile" }
            Unit
        }
    }

    private suspend fun fetchProfilePayloadBySession(session: AuthSession): Result<AuthProfilePayload> =
        tbClientLoginNetworkSource
            .login(
                bduss = session.bduss,
                stoken = session.stoken,
            ).map { raw ->
                raw.toProfilePayload()
            }

    private suspend fun fetchProfilePayloadByActiveSession(): Result<AuthProfilePayload> {
        val session =
            authStore.state.value.activeAccount
                ?.session
            ?: return Result.failure(IllegalStateException("no active session"))
        return fetchProfilePayloadBySession(session)
    }

    private suspend fun fetchProfilePayloadByActiveCookie(): Result<AuthProfilePayload> =
        webMyInfoNetworkSource
            .fetchMyInfo()
            .map { raw ->
                raw.toProfilePayload()
            }
}
