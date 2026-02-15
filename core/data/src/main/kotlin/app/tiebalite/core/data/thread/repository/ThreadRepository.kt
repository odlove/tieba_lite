package app.tiebalite.core.data.thread.repository

import app.tiebalite.core.model.thread.ThreadPage

interface ThreadRepository {
    suspend fun loadThreadPage(
        threadId: Long,
        page: Int = 1,
    ): Result<ThreadPage>
}
