package app.tiebalite.core.data.forum.repository

import app.tiebalite.core.model.forum.ForumPage

interface ForumRepository {
    suspend fun loadForumPage(
        forumName: String,
        page: Int = 1,
        loadType: Int = 1,
        sortType: Int = 0,
    ): Result<ForumPage>
}
