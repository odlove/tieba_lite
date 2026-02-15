package app.tiebalite.core.data.auth.bridge

import app.tiebalite.core.data.auth.store.AuthStore
import app.tiebalite.core.network.source.web.WebCookieJarStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class AuthWebCookieJarStore(
    private val authStore: AuthStore,
    private val scope: CoroutineScope,
) : WebCookieJarStore {
    private val lock = Any()
    private val pendingCookiesByAccountId = mutableMapOf<String, String>()
    private val saveJobsByAccountId = mutableMapOf<String, Job>()

    override fun loadRawCookie(): String? {
        val activeAccountId =
            authStore.state.value.activeAccountId
                ?: return null
        return authStore.cookieOf(activeAccountId)
    }

    override fun saveRawCookie(rawCookie: String) {
        val accountId =
            authStore.state.value.activeAccountId
                ?: return
        val normalizedCookie = rawCookie.trim()
        if (normalizedCookie.isBlank()) {
            return
        }
        val shouldScheduleJob =
            synchronized(lock) {
                val pendingCookie = pendingCookiesByAccountId[accountId]
                val persistedCookie = authStore.cookieOf(accountId)
                if (normalizedCookie == pendingCookie || normalizedCookie == persistedCookie) {
                    return@synchronized false
                }
                pendingCookiesByAccountId[accountId] = normalizedCookie
                saveJobsByAccountId[accountId] == null
            }
        if (!shouldScheduleJob) {
            return
        }
        val job =
            scope.launch {
                delay(CookieWriteThrottleMillis)
                val cookieToSave =
                    synchronized(lock) {
                        saveJobsByAccountId.remove(accountId)
                        pendingCookiesByAccountId.remove(accountId)
                    } ?: return@launch
                if (cookieToSave == authStore.cookieOf(accountId)) {
                    return@launch
                }
                authStore.saveCookie(
                    accountId = accountId,
                    rawCookie = cookieToSave,
                )
            }
        synchronized(lock) {
            saveJobsByAccountId[accountId] = job
        }
    }

    private companion object {
        private const val CookieWriteThrottleMillis = 10_000L
    }
}
