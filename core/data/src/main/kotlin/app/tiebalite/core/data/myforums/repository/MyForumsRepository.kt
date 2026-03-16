package app.tiebalite.core.data.myforums.repository

import app.tiebalite.core.model.myforums.MyForumItem

interface MyForumsRepository {
    suspend fun loadMyForums(): Result<List<MyForumItem>>
}
