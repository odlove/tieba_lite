package app.tiebalite.core.data.recommend.mapper

import app.tiebalite.core.data.recommend.model.RecommendItem
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw

class PersonalizedFeedMapper {
    fun map(raw: PersonalizedFeedRaw): List<RecommendItem> {
        return raw.response.data.threadListList.map { thread ->
            val threadId = thread.tid.takeIf { it != 0L } ?: thread.id
            val authorName =
                thread.author.nameShow.ifBlank {
                    thread.author.name
                }
            val subtitleParts =
                buildList {
                    if (thread.fname.isNotBlank()) {
                        add(thread.fname)
                    }
                    if (authorName.isNotBlank()) {
                        add(authorName)
                    }
                    if (thread.replyNum > 0) {
                        add("回复 ${thread.replyNum}")
                    }
                }
            RecommendItem(
                id = threadId.toString(),
                title = thread.title.ifBlank { "(无标题)" },
                subtitle = subtitleParts.joinToString(separator = " · ").ifBlank { null },
            )
        }
    }
}
