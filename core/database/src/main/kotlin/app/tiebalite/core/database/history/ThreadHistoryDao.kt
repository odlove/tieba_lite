package app.tiebalite.core.database.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadHistoryDao {
    @Query(
        """
        SELECT * FROM thread_history
        ORDER BY last_entered_at DESC
        """,
    )
    fun observeThreadHistory(): Flow<List<ThreadHistoryEntity>>

    @Insert
    suspend fun insertVisitLog(entity: ThreadVisitLogEntity): Long

    @Upsert
    suspend fun upsertThreadHistory(entity: ThreadHistoryEntity)

    @Query(
        """
        UPDATE thread_visit_log
        SET left_at = :leftAt
        WHERE id = :visitLogId
        """,
    )
    suspend fun updateVisitLogLeftAt(
        visitLogId: Long,
        leftAt: Long,
    )

    @Query(
        """
        DELETE FROM thread_history
        WHERE thread_id = :threadId
        """,
    )
    suspend fun deleteThreadHistory(threadId: Long)

    @Query("DELETE FROM thread_history")
    suspend fun clearThreadHistory()

    @Transaction
    suspend fun recordThreadEntered(
        visitLog: ThreadVisitLogEntity,
        history: ThreadHistoryEntity,
    ): Long {
        val visitLogId = insertVisitLog(visitLog)
        upsertThreadHistory(history)
        return visitLogId
    }

    @Transaction
    suspend fun recordThreadLeft(
        visitLogId: Long,
        leftAt: Long,
    ) {
        updateVisitLogLeftAt(
            visitLogId = visitLogId,
            leftAt = leftAt,
        )
    }
}
