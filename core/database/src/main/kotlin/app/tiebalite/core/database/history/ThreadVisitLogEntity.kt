package app.tiebalite.core.database.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "thread_visit_log",
    indices = [
        Index(value = ["thread_id"]),
        Index(value = ["entered_at"]),
    ],
)
data class ThreadVisitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
    @ColumnInfo(name = "entered_at")
    val enteredAt: Long,
    @ColumnInfo(name = "left_at")
    val leftAt: Long? = null,
)
