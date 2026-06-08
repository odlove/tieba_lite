package app.tiebalite.core.model.thread

import app.tiebalite.core.model.text.RichTextPart

data class ThreadPostBody(
    val inline: List<RichTextPart> = emptyList(),
    val media: List<MediaPart> = emptyList(),
) {
    sealed interface MediaPart {
        data class Image(
            val url: String,
            val width: Int? = null,
            val height: Int? = null,
        ) : MediaPart

        data class Video(
            val coverUrl: String? = null,
            val videoUrl: String? = null,
            val webUrl: String? = null,
        ) : MediaPart

        data class Voice(
            val voiceMd5: String,
            val durationSeconds: Int = 0,
        ) : MediaPart
    }
}
