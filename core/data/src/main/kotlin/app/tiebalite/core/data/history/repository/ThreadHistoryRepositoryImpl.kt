package app.tiebalite.core.data.history.repository

import app.tiebalite.core.database.history.ThreadHistoryDao
import app.tiebalite.core.database.history.ThreadHistoryEntity
import app.tiebalite.core.database.history.ThreadVisitLogEntity
import app.tiebalite.core.model.history.ThreadHistoryEntry
import app.tiebalite.core.model.history.ThreadHistoryRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThreadHistoryRepositoryImpl(
    private val dao: ThreadHistoryDao,
) : ThreadHistoryRepository {
    override suspend fun onThreadEntered(entry: ThreadHistoryEntry): Long {
        val enteredAt = System.currentTimeMillis()
        return dao.recordThreadEntered(
            visitLog =
                ThreadVisitLogEntity(
                    threadId = entry.threadId,
                    title = entry.title,
                    authorName = entry.authorName,
                    authorAvatarUrl = entry.authorAvatarUrl,
                    forumId = entry.forumId,
                    forumName = entry.forumName,
                    forumAvatarUrl = entry.forumAvatarUrl,
                    enteredAt = enteredAt,
                ),
            history =
                ThreadHistoryEntity(
                    threadId = entry.threadId,
                    title = entry.title,
                    authorName = entry.authorName,
                    authorAvatarUrl = entry.authorAvatarUrl,
                    forumId = entry.forumId,
                    forumName = entry.forumName,
                    forumAvatarUrl = entry.forumAvatarUrl,
                    lastEnteredAt = enteredAt,
                ),
        )
    }

    override suspend fun onThreadLeft(
        visitLogId: Long,
    ) {
        dao.recordThreadLeft(
            visitLogId = visitLogId,
            leftAt = System.currentTimeMillis(),
        )
    }

    override fun observeThreadHistory(): Flow<List<ThreadHistoryRecord>> =
        dao.observeThreadHistory().map { entities ->
            entities.map { entity ->
                ThreadHistoryRecord(
                    threadId = entity.threadId,
                    title = entity.title,
                    authorName = entity.authorName,
                    authorAvatarUrl = entity.authorAvatarUrl,
                    forumId = entity.forumId,
                    forumName = entity.forumName,
                    forumAvatarUrl = entity.forumAvatarUrl,
                    lastEnteredAt = entity.lastEnteredAt,
                )
            }
        }

    override suspend fun deleteThreadHistory(threadId: Long) {
        dao.deleteThreadHistory(threadId)
    }

    override suspend fun clearThreadHistory() {
        dao.clearThreadHistory()
    }
}
