package app.tiebalite.core.model.thread

data class ThreadPostBody(
    val inline: List<InlinePart> = emptyList(),
    val media: List<MediaPart> = emptyList(),
) {
    sealed interface InlinePart {
        data class Text(
            val text: String,
        ) : InlinePart

        data class Link(
            val text: String,
            val url: String,
        ) : InlinePart

        data class Mention(
            val text: String,
            val uid: Long? = null,
        ) : InlinePart

        data class Emoticon(
            val name: String,
            val id: String? = null,
        ) : InlinePart

        data class Unknown(
            val type: Int,
            val text: String,
            val link: String,
        ) : InlinePart
    }

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
