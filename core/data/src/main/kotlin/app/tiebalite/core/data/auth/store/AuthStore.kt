package app.tiebalite.core.data.auth.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import app.tiebalite.core.model.auth.AuthAccount
import app.tiebalite.core.model.auth.AuthProfile
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.proto.auth.AccountCookieProto
import app.tiebalite.core.proto.auth.AuthAccountProto
import app.tiebalite.core.proto.auth.AuthStoreProto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

internal class AuthStore private constructor(
    appContext: Context,
) {
    private val dataScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore: DataStore<AuthStoreProto> =
        DataStoreFactory.create(
            serializer = AuthStoreSerializer,
            scope = dataScope,
            produceFile = {
                File(
                    appContext.filesDir,
                    "datastore/auth_store.pb",
                ).apply {
                    parentFile?.mkdirs()
                }
            },
        )

    private val mutableState = MutableStateFlow(AuthStoreSnapshot())

    internal val state: StateFlow<AuthStoreSnapshot> = mutableState.asStateFlow()

    init {
        dataScope.launch {
            dataStore.data.collect { proto ->
                val accounts =
                    proto.accountsList
                        .asSequence()
                        .map { account ->
                            account.toModel()
                        }.sortedByDescending { account ->
                            account.updatedAtMillis
                        }.toList()
                val activeAccountId = proto.activeAccountId.takeIf { it.isNotBlank() }
                val cookies =
                    proto.cookiesList
                        .asSequence()
                        .filter { cookie ->
                            cookie.accountId.isNotBlank() && cookie.rawCookie.isNotBlank()
                        }.associate { cookie ->
                            cookie.accountId to cookie.rawCookie
                        }
                mutableState.value =
                    AuthStoreSnapshot(
                        accounts = accounts,
                        activeAccountId = activeAccountId,
                        cookies = cookies,
                    )
            }
        }
    }

    internal suspend fun upsertAccount(
        session: AuthSession,
        activate: Boolean = true,
    ): String {
        var savedAccountId = ""
        dataStore.updateData { current ->
            val existingIndex = current.accountsList.indexOfFirst { it.bduss == session.bduss }
            val accountId =
                if (existingIndex >= 0) {
                    current.accountsList[existingIndex].accountId
                } else {
                    UUID.randomUUID().toString()
                }
            savedAccountId = accountId
            val preservedAccount = current.accountsList.getOrNull(existingIndex)
            val preservedProfile =
                preservedAccount
                    ?.toProfileOrNull()
            val sessionTbs =
                session.tbs
                    ?.takeIf { it.isNotBlank() }
                    ?: preservedAccount
                        ?.tbs
                        ?.takeIf { it.isNotBlank() }
            val updatedAccount =
                AuthAccountProto
                    .newBuilder()
                    .setAccountId(accountId)
                    .setBduss(session.bduss)
                    .setStoken(session.stoken)
                    .setTbs(sessionTbs.orEmpty())
                    .setUpdatedAtMillis(System.currentTimeMillis())
                    .apply {
                        preservedProfile?.let { profile ->
                            userId = profile.userId
                            userName = profile.userName
                            displayName = profile.displayName
                            avatarUrl = profile.avatarUrl
                        }
                    }.build()
            val updatedAccounts =
                current.accountsList
                    .toMutableList()
                    .apply {
                        if (existingIndex >= 0) {
                            set(existingIndex, updatedAccount)
                        } else {
                            add(updatedAccount)
                        }
                    }.sortedByDescending { account ->
                        account.updatedAtMillis
                    }
            current
                .toBuilder()
                .clearAccounts()
                .addAllAccounts(updatedAccounts)
                .apply {
                    if (activate) {
                        activeAccountId = accountId
                    }
                }.build()
        }
        return savedAccountId
    }

    internal suspend fun setActiveAccount(accountId: String?): Boolean {
        var updated = false
        dataStore.updateData { current ->
            if (accountId.isNullOrBlank()) {
                updated = true
                current
                    .toBuilder()
                    .clearActiveAccountId()
                    .build()
            } else if (current.accountsList.none { it.accountId == accountId }) {
                current
            } else {
                updated = true
                current
                    .toBuilder()
                    .setActiveAccountId(accountId)
                    .build()
            }
        }
        return updated
    }

    internal suspend fun removeAccount(accountId: String): Boolean {
        var removed = false
        dataStore.updateData { current ->
            if (current.accountsList.none { it.accountId == accountId }) {
                return@updateData current
            }
            removed = true
            val updatedAccounts =
                current.accountsList
                    .asSequence()
                    .filterNot { it.accountId == accountId }
                    .sortedByDescending { it.updatedAtMillis }
                    .toList()
            val updatedCookies =
                current.cookiesList
                    .asSequence()
                    .filterNot { it.accountId == accountId }
                    .toList()
            current
                .toBuilder()
                .clearAccounts()
                .addAllAccounts(updatedAccounts)
                .clearCookies()
                .addAllCookies(updatedCookies)
                .apply {
                    if (current.activeAccountId == accountId) {
                        val fallbackAccountId = updatedAccounts.firstOrNull()?.accountId.orEmpty()
                        if (fallbackAccountId.isBlank()) {
                            clearActiveAccountId()
                        } else {
                            activeAccountId = fallbackAccountId
                        }
                    }
                }.build()
        }
        return removed
    }

    internal fun cookieOf(accountId: String): String? =
        mutableState.value.cookies[accountId]
            ?.takeIf { it.isNotBlank() }

    internal suspend fun saveCookie(
        accountId: String,
        rawCookie: String,
    ) {
        if (rawCookie.isBlank()) {
            return
        }
        dataStore.updateData { current ->
            val updatedCookies =
                current.cookiesList
                    .toMutableList()
                    .apply {
                        val index = indexOfFirst { it.accountId == accountId }
                        val updated =
                            AccountCookieProto
                                .newBuilder()
                                .setAccountId(accountId)
                                .setRawCookie(rawCookie)
                                .build()
                        if (index >= 0) {
                            set(index, updated)
                        } else {
                            add(updated)
                        }
                    }
            current
                .toBuilder()
                .clearCookies()
                .addAllCookies(updatedCookies)
                .build()
        }
    }

    internal suspend fun saveProfile(
        accountId: String,
        profile: AuthProfile,
        tbs: String? = null,
    ): Boolean {
        var updated = false
        dataStore.updateData { current ->
            val existingIndex = current.accountsList.indexOfFirst { it.accountId == accountId }
            if (existingIndex < 0) {
                return@updateData current
            }
            val existing = current.accountsList[existingIndex]
            val normalizedTbs = tbs?.takeIf { it.isNotBlank() }
            val updatedAccount =
                existing
                    .toBuilder()
                    .setUserId(profile.userId)
                    .setUserName(profile.userName)
                    .setDisplayName(profile.displayName)
                    .setAvatarUrl(profile.avatarUrl)
                    .apply {
                        if (normalizedTbs != null) {
                            setTbs(normalizedTbs)
                        }
                    }
                    .build()
            val updatedAccounts =
                current.accountsList
                    .toMutableList()
                    .apply {
                        set(existingIndex, updatedAccount)
                    }
            updated = true
            current
                .toBuilder()
                .clearAccounts()
                .addAllAccounts(updatedAccounts)
                .build()
        }
        return updated
    }

    companion object {
        @Volatile
        private var instance: AuthStore? = null

        internal fun get(context: Context): AuthStore =
            instance ?: synchronized(this) {
                instance ?: AuthStore(context.applicationContext).also { created ->
                    instance = created
                }
            }
    }
}

internal data class AuthStoreSnapshot(
    val accounts: List<AuthAccount> = emptyList(),
    val activeAccountId: String? = null,
    val cookies: Map<String, String> = emptyMap(),
) {
    val activeAccount: AuthAccount?
        get() =
            activeAccountId
                ?.let { id ->
                    accounts.firstOrNull { account ->
                        account.accountId == id
                    }
                }
}
