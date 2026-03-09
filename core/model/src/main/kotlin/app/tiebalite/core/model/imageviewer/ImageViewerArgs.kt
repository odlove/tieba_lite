package app.tiebalite.core.model.imageviewer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageViewerArgs(
    val items: List<ImageViewerItem>,
    val initialIndex: Int = 0,
) : Parcelable

@Parcelize
data class ImageViewerItem(
    val id: String,
    val imageUrl: String,
    val width: Int? = null,
    val height: Int? = null,
) : Parcelable
