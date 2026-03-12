package app.tiebalite.core.data.history.repository

import app.tiebalite.core.model.history.ThreadHistoryEntry
import app.tiebalite.core.model.history.ThreadHistoryRecord
import kotlinx.coroutines.flow.Flow

interface ThreadHistoryRepository {
    suspend fun onThreadEntered(entry: ThreadHistoryEntry): Long

    suspend fun onThreadLeft(visitLogId: Long)

    fun observeThreadHistory(): Flow<List<ThreadHistoryRecord>>

    suspend fun deleteThreadHistory(threadId: Long)

    suspend fun clearThreadHistory()
}
