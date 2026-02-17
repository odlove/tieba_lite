package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadPostBody
import app.tiebalite.core.network.proto.thread.ThreadPbContentLite

internal class ThreadContentMapper {
    fun map(contentList: List<ThreadPbContentLite>): ThreadPostBody {
        val inline = mutableListOf<ThreadPostBody.InlinePart>()
        val media = mutableListOf<ThreadPostBody.MediaPart>()
        contentList.forEach { content ->
            mapInline(content)?.let(inline::add)
            mapMedia(content)?.let(media::add)
        }
        return ThreadPostBody(
            inline = inline.toList(),
            media = media.toList(),
        )
    }

    private fun mapInline(content: ThreadPbContentLite): ThreadPostBody.InlinePart? {
        val type = content.type
        val text = content.text
        val link = content.link
        return when (type) {
            0, 9, 27, 35, 40 -> {
                text.takeIf { it.isNotEmpty() }?.let(ThreadPostBody.InlinePart::Text)
            }

            1 -> {
                val normalizedLink = normalizeUrl(link.trim()) ?: link.takeIf { it.isNotBlank() }
                when {
                    normalizedLink != null -> ThreadPostBody.InlinePart.Link(
                        text = text.ifEmpty { normalizedLink },
                        url = normalizedLink,
                    )

                    text.isNotEmpty() -> ThreadPostBody.InlinePart.Text(text)
                    else -> null
                }
            }

            2 -> {
                val emoticonId =
                    content.text
                        .trim()
                        .takeIf { it.isNotEmpty() }
                        ?.let(::normalizeEmoticonId)
                val emoticonName =
                    content.c
                        .trim()
                        .takeIf { it.isNotEmpty() }
                if (emoticonId == null && emoticonName == null) {
                    null
                } else {
                    ThreadPostBody.InlinePart.Emoticon(
                        name = emoticonName ?: emoticonId.orEmpty(),
                        id = emoticonId,
                    )
                }
            }

            4 -> {
                text.takeIf { it.isNotEmpty() }?.let {
                    ThreadPostBody.InlinePart.Mention(
                        text = it,
                        uid = content.uid.takeIf { uid -> uid > 0L },
                    )
                }
            }

            else -> {
                if (type in MediaTypes || (text.isEmpty() && link.isEmpty())) {
                    null
                } else {
                    ThreadPostBody.InlinePart.Unknown(
                        type = type,
                        text = text,
                        link = link,
                    )
                }
            }
        }
    }

    private fun mapMedia(content: ThreadPbContentLite): ThreadPostBody.MediaPart? =
        when (content.type) {
            3, 20 -> {
                val imageUrl = pickImageUrl(content)
                val parsedImageSize = parseImageSize(content.bsize)
                imageUrl?.let {
                    ThreadPostBody.MediaPart.Image(
                        url = it,
                        width = parsedImageSize?.first,
                        height = parsedImageSize?.second,
                    )
                }
            }

            5 -> {
                val text = content.text.trim()
                val link = content.link.trim()
                val coverUrl = normalizeUrl(content.src)
                val videoUrl = normalizeUrl(link) ?: link.takeIf { it.isNotBlank() }
                val webUrl = normalizeUrl(text) ?: text.takeIf { it.isNotBlank() }
                if (coverUrl == null && videoUrl == null && webUrl == null) {
                    null
                } else {
                    ThreadPostBody.MediaPart.Video(
                        coverUrl = coverUrl,
                        videoUrl = videoUrl,
                        webUrl = webUrl,
                    )
                }
            }

            10 -> {
                val voiceMd5 = content.voiceMd5.trim()
                voiceMd5.takeIf { it.isNotBlank() }?.let {
                    ThreadPostBody.MediaPart.Voice(
                        voiceMd5 = it,
                        durationSeconds = content.duringTime.coerceAtLeast(0),
                    )
                }
            }

            else -> null
        }

    private fun normalizeEmoticonId(rawId: String): String = if (rawId == "image_emoticon") "image_emoticon1" else rawId

    private fun parseImageSize(rawSize: String): Pair<Int, Int>? {
        val values = rawSize.split(',')
        if (values.size != 2) {
            return null
        }
        val width = values[0].trim().toIntOrNull()
        val height = values[1].trim().toIntOrNull()
        if (width == null || height == null || width <= 0 || height <= 0) {
            return null
        }
        return width to height
    }

    private fun pickImageUrl(content: ThreadPbContentLite): String? =
        sequenceOf(
            content.originSrc,
            content.bigCdnSrc,
            content.cdnSrc,
            content.src,
        ).mapNotNull(::normalizeUrl).firstOrNull()

    private companion object {
        val MediaTypes = setOf(3, 5, 10, 20)
    }
}
