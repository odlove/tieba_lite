package app.tiebalite.core.data.thread.repository

import app.tiebalite.core.model.thread.ThreadPage
import app.tiebalite.core.model.thread.ThreadSubPostsPage

interface ThreadRepository {
    suspend fun loadThreadPage(
        threadId: Long,
        page: Int = 1,
        postId: Long = 0L,
        lastPostId: Long? = null,
    ): Result<ThreadPage>

    suspend fun loadThreadSubPostsPage(
        threadId: Long,
        postId: Long,
        page: Int = 1,
        subPostId: Long = 0L,
        forumId: Long = 0L,
    ): Result<ThreadSubPostsPage>
}
