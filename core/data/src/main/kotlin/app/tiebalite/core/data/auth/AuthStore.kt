package app.tiebalite.core.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import app.tiebalite.core.model.auth.AuthAccount
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

class AuthStore private constructor(
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

    private val mutableAccounts = MutableStateFlow(emptyList<AuthAccount>())
    private val mutableActiveAccountId = MutableStateFlow<String?>(null)
    private val mutableCookies = MutableStateFlow(emptyMap<String, String>())

    val accounts: StateFlow<List<AuthAccount>> = mutableAccounts.asStateFlow()
    val activeAccountId: StateFlow<String?> = mutableActiveAccountId.asStateFlow()

    init {
        dataScope.launch {
            dataStore.data.collect { proto ->
                mutableAccounts.value =
                    proto.accountsList
                        .asSequence()
                        .map { account ->
                            account.toModel()
                        }.sortedByDescending { account ->
                            account.updatedAtMillis
                        }.toList()
                mutableActiveAccountId.value = proto.activeAccountId.takeIf { it.isNotBlank() }
                mutableCookies.value =
                    proto.cookiesList
                        .asSequence()
                        .filter { cookie ->
                            cookie.accountId.isNotBlank() && cookie.rawCookie.isNotBlank()
                        }.associate { cookie ->
                            cookie.accountId to cookie.rawCookie
                        }
            }
        }
    }

    fun currentSession(): AuthSession? = currentActiveAccount()?.session

    fun currentActiveAccountId(): String? = mutableActiveAccountId.value

    fun currentActiveAccount(): AuthAccount? {
        val activeId = mutableActiveAccountId.value ?: return null
        return mutableAccounts.value.firstOrNull { it.accountId == activeId }
    }

    suspend fun upsertAccount(
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
            val updatedAccount =
                AuthAccountProto
                    .newBuilder()
                    .setAccountId(accountId)
                    .setBduss(session.bduss)
                    .setStoken(session.stoken)
                    .setUpdatedAtMillis(System.currentTimeMillis())
                    .build()
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

    suspend fun setActiveAccount(accountId: String?): Boolean {
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

    suspend fun removeAccount(accountId: String): Boolean {
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

    suspend fun removeActiveAccount(): Boolean {
        val activeId = mutableActiveAccountId.value ?: return false
        return removeAccount(activeId)
    }

    suspend fun clearActiveAccount() {
        setActiveAccount(null)
    }

    fun cookieOf(accountId: String): String? =
        mutableCookies.value[accountId]
            ?.takeIf { it.isNotBlank() }

    fun cookieOfActiveAccount(): String? {
        val activeId = mutableActiveAccountId.value ?: return null
        return cookieOf(activeId)
    }

    suspend fun saveCookie(
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

    suspend fun removeCookie(accountId: String) {
        dataStore.updateData { current ->
            if (current.cookiesList.none { it.accountId == accountId }) {
                return@updateData current
            }
            val updatedCookies =
                current.cookiesList
                    .asSequence()
                    .filterNot { it.accountId == accountId }
                    .toList()
            current
                .toBuilder()
                .clearCookies()
                .addAllCookies(updatedCookies)
                .build()
        }
    }

    companion object {
        @Volatile
        private var instance: AuthStore? = null

        fun get(context: Context): AuthStore =
            instance ?: synchronized(this) {
                instance ?: AuthStore(context.applicationContext).also { created ->
                    instance = created
                }
            }
    }
}

private fun AuthAccountProto.toModel(): AuthAccount =
    AuthAccount(
        accountId = accountId,
        session =
            AuthSession(
                bduss = bduss,
                stoken = stoken,
            ),
        updatedAtMillis = updatedAtMillis,
    )
