package app.tiebalite.core.data.recommend.mapper

import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw

class PersonalizedFeedMapper {
    fun map(raw: PersonalizedFeedRaw): List<RecommendItem> {
        return raw.response.data.threadListList.map { thread ->
            val threadId = thread.tid.takeIf { it != 0L } ?: thread.id
            val authorName =
                thread.author.nameShow.ifBlank {
                    thread.author.name
                }
            val snippet =
                thread.abstractItemsList
                    .asSequence()
                    .map { it.text.trim() }
                    .firstOrNull { it.isNotBlank() }

            val coverImageUrl =
                thread.mediaList
                    .asSequence()
                    .mapNotNull { media ->
                        normalizeUrl(media.originPic)
                    }
                    .firstOrNull()
            RecommendItem(
                id = threadId.toString(),
                title = thread.title.ifBlank { "(无标题)" },
                forumName = thread.fname.ifBlank { null },
                snippet = snippet,
                authorName = authorName.ifBlank { null },
                authorAvatarUrl = portraitToAvatarUrl(thread.author.portrait),
                coverImageUrl = coverImageUrl,
                replyCount = thread.replyNum,
                agreeCount = thread.agreeNum,
                shareCount = thread.shareNum,
                lastTimeTimestampSeconds =
                    thread.lastTimeInt
                        .takeIf { it > 0 }
                        ?.toLong()
                        ?: thread.createTime
                            .takeIf { it > 0 }
                            ?.toLong(),
            )
        }
    }

    private fun portraitToAvatarUrl(portrait: String): String? {
        val value = portrait.trim()
        if (value.isBlank()) {
            return null
        }
        return if (value.startsWith("http://") || value.startsWith("https://")) {
            value
        } else {
            "http://tb.himg.baidu.com/sys/portrait/item/$value"
        }
    }

    private fun normalizeUrl(raw: String): String? {
        val value = raw.trim()
        if (value.isBlank()) {
            return null
        }
        return when {
            value.startsWith("http://") -> "https://${value.removePrefix("http://")}"
            value.startsWith("https://") -> value
            value.startsWith("//") -> "https:$value"
            else -> value
        }
    }
}
