package app.tiebalite.core.data.auth.di

import android.content.Context
import app.tiebalite.core.data.auth.service.AuthReader
import app.tiebalite.core.data.auth.service.AuthService
import app.tiebalite.core.data.auth.service.DefaultAuthReader
import app.tiebalite.core.data.auth.store.AuthStore
import kotlinx.coroutines.CoroutineScope

interface AuthGraph {
    val authReader: AuthReader
    val authService: AuthService
}

interface AuthGraphProvider {
    val authGraph: AuthGraph
}

class DefaultAuthGraph(
    context: Context,
    scope: CoroutineScope,
) : AuthGraph {
    private val authStore: AuthStore = AuthStore.get(context.applicationContext)

    override val authReader: AuthReader = DefaultAuthReader(authStore = authStore, scope = scope)
    override val authService: AuthService = AuthServiceFactory.create(authStore = authStore, scope = scope)
}
