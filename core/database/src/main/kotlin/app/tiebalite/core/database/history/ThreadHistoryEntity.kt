package app.tiebalite.core.database.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "thread_history",
    indices = [
        Index(value = ["last_entered_at"]),
    ],
)
data class ThreadHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "thread_id")
    val threadId: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "author_name")
    val authorName: String? = null,
    @ColumnInfo(name = "author_avatar_url")
    val authorAvatarUrl: String? = null,
    @ColumnInfo(name = "forum_id")
    val forumId: Long? = null,
    @ColumnInfo(name = "forum_name")
    val forumName: String? = null,
    @ColumnInfo(name = "forum_avatar_url")
    val forumAvatarUrl: String? = null,
    @ColumnInfo(name = "last_entered_at")
    val lastEnteredAt: Long,
)
