package app.tiebalite.core.data.auth

import app.tiebalite.core.network.source.web.WebCookieJarStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AuthWebCookieJarStore(
    private val authStore: AuthStore,
    private val scope: CoroutineScope,
) : WebCookieJarStore {
    override fun loadRawCookie(): String? = authStore.cookieOfActiveAccount()

    override fun saveRawCookie(rawCookie: String) {
        val accountId = authStore.currentActiveAccountId() ?: return
        if (rawCookie.isBlank()) {
            return
        }
        scope.launch {
            authStore.saveCookie(
                accountId = accountId,
                rawCookie = rawCookie,
            )
        }
    }
}
