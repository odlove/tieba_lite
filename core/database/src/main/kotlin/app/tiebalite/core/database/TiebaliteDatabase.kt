package app.tiebalite.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.tiebalite.core.database.history.ThreadHistoryDao
import app.tiebalite.core.database.history.ThreadHistoryEntity
import app.tiebalite.core.database.history.ThreadVisitLogEntity

@Database(
    entities = [ThreadVisitLogEntity::class, ThreadHistoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TiebaliteDatabase : RoomDatabase() {
    abstract fun threadHistoryDao(): ThreadHistoryDao

    companion object {
        @Volatile
        private var instance: TiebaliteDatabase? = null

        fun getInstance(context: Context): TiebaliteDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }

        private fun buildDatabase(context: Context): TiebaliteDatabase =
            Room
                .databaseBuilder(
                    context,
                    TiebaliteDatabase::class.java,
                    DATABASE_NAME,
                )
                .build()

        private const val DATABASE_NAME = "tiebalite.db"
    }
}
