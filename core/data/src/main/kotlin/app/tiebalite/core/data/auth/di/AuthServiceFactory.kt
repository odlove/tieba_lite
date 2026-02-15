package app.tiebalite.core.data.auth.di

import app.tiebalite.core.data.auth.bridge.AuthWebCookieJarStore
import app.tiebalite.core.data.auth.service.AuthService
import app.tiebalite.core.data.auth.service.DefaultAuthService
import app.tiebalite.core.data.auth.store.AuthStore
import app.tiebalite.core.network.source.web.auth.WebAuthNetwork
import kotlinx.coroutines.CoroutineScope

internal object AuthServiceFactory {
    internal fun create(
        authStore: AuthStore,
        scope: CoroutineScope,
    ): AuthService {
        val webCookieJarStore =
            AuthWebCookieJarStore(
                authStore = authStore,
                scope = scope,
            )
        val webMyInfoNetworkSource = WebAuthNetwork.createMyInfoNetworkSource(store = webCookieJarStore)
        return DefaultAuthService(
            authStore = authStore,
            webMyInfoNetworkSource = webMyInfoNetworkSource,
        )
    }
}
