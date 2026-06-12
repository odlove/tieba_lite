package app.tiebalite.core.data.recommend.mapper

import app.tiebalite.core.data.common.mapper.normalizeUrl
import app.tiebalite.core.data.common.mapper.portraitToAvatarUrl
import app.tiebalite.core.data.common.mapper.toRecommendImage
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.model.recommend.RecommendVideo
import app.tiebalite.core.model.text.RichText
import app.tiebalite.core.network.proto.recommend.VideoInfoLite
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

            val images =
                thread.mediaList
                    .asSequence()
                    .mapNotNull { media -> media.toRecommendImage() }
                    .distinctBy { image -> image.url }
                    .toList()
            val video = thread.videoInfo.toRecommendVideo()
            val forumName =
                thread.forumInfo.name
                    .ifBlank { thread.fname }
                    .ifBlank { null }
            RecommendItem(
                id = threadId.toString(),
                title = RichText.text(thread.title.ifBlank { "(无标题)" }),
                forumName = forumName,
                forumAvatarUrl = normalizeUrl(thread.forumInfo.avatar),
                snippet = snippet?.let(RichText::text),
                authorName = authorName.ifBlank { null },
                authorAvatarUrl = portraitToAvatarUrl(thread.author.portrait),
                images = images,
                video = video,
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
                isTop = thread.isTop == 1,
            )
        }
    }
}

private fun VideoInfoLite.toRecommendVideo(): RecommendVideo? {
    val url = normalizeUrl(videoUrl) ?: return null
    val coverUrl = normalizeUrl(thumbnailUrl) ?: return null
    return RecommendVideo(
        url = url,
        coverUrl = coverUrl,
        width = videoWidth.takeIf { it > 0 },
        height = videoHeight.takeIf { it > 0 },
        durationSeconds = videoDuration.takeIf { it > 0 },
    )
}
