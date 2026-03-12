package app.tiebalite.core.data.di

import kotlinx.coroutines.CoroutineScope

interface ApplicationScopeProvider {
    val applicationScope: CoroutineScope
}
