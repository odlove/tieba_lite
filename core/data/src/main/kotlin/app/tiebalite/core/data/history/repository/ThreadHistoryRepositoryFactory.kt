package app.tiebalite.core.data.history.repository

import android.content.Context
import app.tiebalite.core.database.history.ThreadHistoryDaoFactory

object ThreadHistoryRepositoryFactory {
    fun create(
        context: Context,
    ): ThreadHistoryRepository {
        return ThreadHistoryRepositoryImpl(
            dao = ThreadHistoryDaoFactory.create(context.applicationContext),
        )
    }
}
