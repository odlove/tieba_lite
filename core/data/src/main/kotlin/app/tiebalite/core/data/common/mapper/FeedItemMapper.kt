package app.tiebalite.core.data.common.mapper

import app.tiebalite.core.model.recommend.RecommendImage
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.model.recommend.RecommendVideo
import app.tiebalite.core.model.text.RichText
import app.tiebalite.core.model.text.RichTextPart
import app.tiebalite.core.network.proto.feed.FeedLite
import app.tiebalite.core.network.proto.feed.FeedPicLite
import app.tiebalite.core.network.proto.feed.FeedTextGroupLite
import app.tiebalite.core.network.proto.feed.FeedVideoInfoLite
import app.tiebalite.core.network.proto.recommend.UserLite

internal data class FeedItemMappingContext(
    val userMap: Map<Long, UserLite> = emptyMap(),
    val threadAuthorMap: Map<Long, UserLite> = emptyMap(),
    val includeForum: Boolean = false,
)

internal fun FeedLite.toRecommendItem(context: FeedItemMappingContext = FeedItemMappingContext()): RecommendItem? {
    val businessInfo = businessInfoList.associate { item -> item.key to item.value }
    val social =
        componentsList
            .asSequence()
            .firstOrNull { component -> component.component == SOCIAL_COMPONENT }
            ?.feedSocial
    val threadId =
        social
            ?.tid
            ?.takeIf { it > 0L }
            ?: businessInfo["thread_id"]?.toLongOrNull()
            ?: schema.threadIdFromSchema()
            ?: return null
    val author =
        context.threadAuthorMap[threadId]
            ?: businessInfo["user_id"]?.toLongOrNull()?.let(context.userMap::get)
    val title =
        componentRichText(TITLE_COMPONENT)
            ?: businessInfo["title"]?.let(RichText::text)
            ?: return null
    val snippet =
        componentRichText(ABSTRACT_COMPONENT)
            ?: businessInfo["abstract"]?.let(RichText::text)
    val images =
        componentsList
            .asSequence()
            .filter { component -> component.component == PIC_COMPONENT }
            .flatMap { component -> component.feedPic.picsList.asSequence() }
            .mapNotNull { pic -> pic.toRecommendImage() }
            .distinctBy { image -> image.url }
            .toList()
    val video =
        componentsList
            .asSequence()
            .firstOrNull { component -> component.component == VIDEO_COMPONENT }
            ?.feedVideo
            ?.videoInfo
            ?.toRecommendVideo(businessInfo)
    return RecommendItem(
        id = threadId.toString(),
        title = title,
        forumName = businessInfo["forum_name"]?.takeIf { context.includeForum },
        forumAvatarUrl = normalizeUrl(businessInfo["forum_avatar"])?.takeIf { context.includeForum },
        snippet = snippet,
        authorName = resolveAuthorName(author),
        authorAvatarUrl = portraitToAvatarUrl(author?.portrait),
        images = images,
        video = video,
        replyCount = social?.commentNum ?: 0,
        agreeCount = social?.agree?.agreeNum?.toInt() ?: 0,
        shareCount = social?.shareNum?.toLong() ?: 0L,
        lastTimeTimestampSeconds =
            businessInfo["create_time"]
                ?.toLongOrNull()
                ?.takeIf { it > 0L },
        isTop = false,
    )
}

private fun FeedLite.componentRichText(componentName: String): RichText? =
    componentsList
        .asSequence()
        .firstOrNull { component -> component.component == componentName }
        ?.let { component ->
            when (componentName) {
                TITLE_COMPONENT -> component.feedTitle
                ABSTRACT_COMPONENT -> component.feedAbstract
                else -> null
            }
        }
        ?.richText()

private fun FeedTextGroupLite.richText(): RichText? {
    val parts =
        dataList
            .mapNotNull { item ->
                when (item.type) {
                    FEED_TEXT_TYPE, FEED_TAG_TEXT_TYPE ->
                        item.textInfo.text
                            .takeIf { text -> text.isNotEmpty() }
                            ?.let(RichTextPart::Text)

                    FEED_EMOTICON_TYPE -> {
                        val emoticonId =
                            item.emojiInfo.name
                                .trim()
                                .takeIf { name -> name.isNotEmpty() }
                                ?.let(::normalizeEmoticonId)
                        val emoticonName =
                            item.emojiInfo.c
                                .trim()
                                .takeIf { name -> name.isNotEmpty() }
                                ?.let(::normalizeEmoticonName)
                        if (emoticonId == null && emoticonName == null) {
                            null
                        } else {
                            RichTextPart.Emoticon(
                                id = emoticonId,
                                name = emoticonName ?: emoticonId.orEmpty(),
                            )
                        }
                    }

                    else ->
                        item.textInfo.text
                            .takeIf { text -> text.isNotEmpty() }
                            ?.let(RichTextPart::Text)
                }
            }
    return RichText(parts).takeIf { text -> text.isNotBlank() }
}

private fun FeedPicLite.toRecommendImage(): RecommendImage? {
    val url =
        normalizeUrl(originPicUrl)
            ?: normalizeUrl(bigPicUrl)
            ?: normalizeUrl(smallPicUrl)
            ?: return null
    return RecommendImage(
        url = url,
        width = width.takeIf { it > 0 },
        height = height.takeIf { it > 0 },
    )
}

private fun FeedVideoInfoLite.toRecommendVideo(businessInfo: Map<String, String>): RecommendVideo? {
    val url = normalizeUrl(url) ?: return null
    val coverUrl =
        normalizeUrl(thumbnail.url)
            ?: normalizeUrl(firstFrameThumbnail.url)
            ?: normalizeUrl(businessInfo["video_thumbnail_url"])
            ?: normalizeUrl(businessInfo["media_pic_url"])
            ?: return null
    return RecommendVideo(
        url = url,
        coverUrl = coverUrl,
        width = width.takeIf { it > 0 },
        height = height.takeIf { it > 0 },
        durationSeconds = duration.takeIf { it > 0 },
    )
}

private fun resolveAuthorName(author: UserLite?): String? {
    author ?: return null
    return author.nameShow.ifBlank { author.name }.ifBlank { null }
}

private fun String.threadIdFromSchema(): Long? {
    if (isBlank()) {
        return null
    }
    val match = THREAD_ID_PATTERN.find(this) ?: return null
    return match.groupValues[1].toLongOrNull()
}

private fun normalizeEmoticonId(rawId: String): String = if (rawId == "image_emoticon") "image_emoticon1" else rawId

private fun normalizeEmoticonName(rawName: String): String {
    val name = rawName.trim()
    return name
        .removeSurrounding("#(", ")")
        .ifBlank { name }
}

private const val FEED_TEXT_TYPE = 1
private const val FEED_EMOTICON_TYPE = 3
private const val FEED_TAG_TEXT_TYPE = 6
private const val TITLE_COMPONENT = "feed_title"
private const val ABSTRACT_COMPONENT = "feed_abstract"
private const val PIC_COMPONENT = "feed_pic"
private const val SOCIAL_COMPONENT = "feed_social"
private const val VIDEO_COMPONENT = "feed_video"
private val THREAD_ID_PATTERN = Regex("""(?:%22(?:tid|threadId|thread_id)%22%3A|["?&](?:tid|threadId|thread_id)["=:%]+)(\d+)""")
