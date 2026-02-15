package app.tiebalite

import android.app.Application
import app.tiebalite.core.data.auth.di.AuthGraph
import app.tiebalite.core.data.auth.di.AuthGraphProvider
import app.tiebalite.core.data.auth.di.DefaultAuthGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TiebaliteApplication : Application(), AuthGraphProvider {
    private val applicationScope: CoroutineScope by lazy(LazyThreadSafetyMode.NONE) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override val authGraph: AuthGraph by lazy(LazyThreadSafetyMode.NONE) {
        DefaultAuthGraph(
            context = this,
            scope = applicationScope,
        )
    }
}
