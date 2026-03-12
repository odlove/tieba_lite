package app.tiebalite.core.database.history

import android.content.Context
import app.tiebalite.core.database.TiebaliteDatabase

object ThreadHistoryDaoFactory {
    fun create(
        context: Context,
    ): ThreadHistoryDao = TiebaliteDatabase.getInstance(context.applicationContext).threadHistoryDao()
}
