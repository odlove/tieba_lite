package app.tiebalite.core.database.history

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query(
        """
        SELECT * FROM history_records
        WHERE type = :type
        ORDER BY visited_at DESC, visit_count DESC
        """,
    )
    fun observeByType(type: String): Flow<List<HistoryEntity>>

    @Query(
        """
        SELECT * FROM history_records
        WHERE type = :type AND target_id = :targetId
        LIMIT 1
        """,
    )
    suspend fun findByTypeAndTargetId(
        type: String,
        targetId: String,
    ): HistoryEntity?

    @Upsert
    suspend fun upsert(entity: HistoryEntity)

    @Query("DELETE FROM history_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history_records WHERE type = :type")
    suspend fun clearByType(type: String)

    @Query("DELETE FROM history_records")
    suspend fun clearAll()
}
