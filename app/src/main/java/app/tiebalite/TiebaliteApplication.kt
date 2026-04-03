package app.tiebalite

import android.app.Application
import app.tiebalite.core.data.auth.di.AuthGraph
import app.tiebalite.core.data.auth.di.AuthGraphProvider
import app.tiebalite.core.data.auth.di.DefaultAuthGraph
import app.tiebalite.core.data.di.ApplicationScopeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TiebaliteApplication : Application(), AuthGraphProvider, ApplicationScopeProvider {
    override val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override lateinit var authGraph: AuthGraph
        private set

    override fun onCreate() {
        super.onCreate()
        authGraph =
            DefaultAuthGraph(
                context = this,
                scope = applicationScope,
            )
    }
}
