package app.tiebalite.core.database.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_records",
    indices = [
        Index(value = ["type", "target_id"], unique = true),
        Index(value = ["visited_at"]),
    ],
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "target_id")
    val targetId: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "subtitle")
    val subtitle: String? = null,
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,
    @ColumnInfo(name = "author_name")
    val authorName: String? = null,
    @ColumnInfo(name = "forum_name")
    val forumName: String? = null,
    @ColumnInfo(name = "last_read_post_id")
    val lastReadPostId: Long? = null,
    @ColumnInfo(name = "last_read_floor")
    val lastReadFloor: Int? = null,
    @ColumnInfo(name = "see_lz")
    val seeLz: Boolean = false,
    @ColumnInfo(name = "visited_at")
    val visitedAt: Long,
    @ColumnInfo(name = "visit_count")
    val visitCount: Int = 1,
)
