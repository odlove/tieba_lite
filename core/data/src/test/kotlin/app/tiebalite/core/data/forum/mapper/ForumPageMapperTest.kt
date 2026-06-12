package app.tiebalite.core.data.forum.mapper

import app.tiebalite.core.model.text.RichTextPart
import app.tiebalite.core.network.proto.feed.FeedAgreeLite
import app.tiebalite.core.network.proto.feed.FeedComponentLite
import app.tiebalite.core.network.proto.feed.FeedEmojiLite
import app.tiebalite.core.network.proto.feed.FeedHeadImageLite
import app.tiebalite.core.network.proto.feed.FeedHeadLite
import app.tiebalite.core.network.proto.feed.FeedHeadSymbolLite
import app.tiebalite.core.network.proto.feed.FeedHeadTextLite
import app.tiebalite.core.network.proto.feed.FeedKeyValueLite
import app.tiebalite.core.network.proto.feed.FeedLayoutLite
import app.tiebalite.core.network.proto.feed.FeedLite
import app.tiebalite.core.network.proto.feed.FeedPicGroupLite
import app.tiebalite.core.network.proto.feed.FeedPicLite
import app.tiebalite.core.network.proto.feed.FeedSocialLite
import app.tiebalite.core.network.proto.feed.FeedTextGroupLite
import app.tiebalite.core.network.proto.feed.FeedTextLite
import app.tiebalite.core.network.proto.feed.FeedTextResourceLite
import app.tiebalite.core.network.proto.feed.FeedThumbnailLite
import app.tiebalite.core.network.proto.feed.FeedVideoInfoLite
import app.tiebalite.core.network.proto.feed.FeedVideoLite
import app.tiebalite.core.network.proto.frs.FrsPageDataLite
import app.tiebalite.core.network.proto.frs.FrsPageForumInfoLite
import app.tiebalite.core.network.proto.frs.FrsPageInfoLite
import app.tiebalite.core.network.proto.frs.FrsPageResponseDataLite
import app.tiebalite.core.network.proto.frs.FrsPageResponseLite
import app.tiebalite.core.network.proto.recommend.UserLite
import app.tiebalite.core.network.source.tbclient.forum.FrsPageRaw
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForumPageMapperTest {
    private val mapper = ForumPageMapper()

    @Test
    fun mapReadsThreadsFromPageDataFeedList() {
        val response = buildResponse()
        val body = response.toByteArray()
        val page =
            mapper.map(
                raw =
                    FrsPageRaw(
                        body = body,
                        response = response,
                    ),
                requestedForumName = "java",
                fallbackCurrentPage = 1,
            )

        assertEquals("java", page.header.forumName)
        assertEquals(1, page.currentPage)
        assertTrue(page.hasMore)
        assertTrue(page.items.isNotEmpty())

        val firstItem = page.items.first()
        assertEquals("10751626484", firstItem.id)
        assertEquals("「AI 点亮你的超能力」创作大赛", firstItem.titleText)
        assertFalse(firstItem.snippet?.isBlank() ?: true)
        assertEquals("吧吧惊喜官", firstItem.authorName)
        assertEquals(AUTHOR_AVATAR_URL, firstItem.authorAvatarUrl)
        assertEquals(48, firstItem.replyCount)
        assertEquals(109, firstItem.agreeCount)
        assertEquals(20L, firstItem.shareCount)
        assertNotNull(firstItem.lastTimeTimestampSeconds)
        assertNull(firstItem.forumName)
        assertNull(firstItem.forumAvatarUrl)
        assertEquals("https://example.com/origin.jpg", firstItem.images.single().url)
        assertEquals(560, firstItem.images.single().width)
        assertEquals(337, firstItem.images.single().height)
    }

    @Test
    fun mapKeepsFeedEmoticonResourcesInTitle() {
        val response = buildResponse(feed = buildFeed(title = richTextGroup()))
        val page =
            mapper.map(
                raw =
                    FrsPageRaw(
                        body = response.toByteArray(),
                        response = response,
                    ),
                requestedForumName = "java",
                fallbackCurrentPage = 1,
            )

        val title = page.items.single().title

        assertEquals("标题#(泪)后缀", title.plainText)
        assertEquals(
            RichTextPart.Emoticon(id = "image_emoticon9", name = "泪"),
            title.parts[1],
        )
    }

    @Test
    fun mapReadsVideoFromFeedVideoComponent() {
        val response = buildResponse(feed = buildFeed(includeVideo = true))
        val page =
            mapper.map(
                raw =
                    FrsPageRaw(
                        body = response.toByteArray(),
                        response = response,
                    ),
                requestedForumName = "java",
                fallbackCurrentPage = 1,
            )
        val video = page.items.single().video ?: error("video missing")

        assertEquals("https://example.com/video.mp4", video.url)
        assertEquals("https://example.com/thumb.jpg", video.coverUrl)
        assertEquals(960, video.width)
        assertEquals(720, video.height)
        assertEquals(51, video.durationSeconds)
    }

    private fun buildResponse(feed: FeedLite = buildFeed()): FrsPageResponseLite =
        FrsPageResponseLite
            .newBuilder()
            .setData(
                FrsPageResponseDataLite
                    .newBuilder()
                    .setForum(
                        FrsPageForumInfoLite
                            .newBuilder()
                            .setId(693735)
                            .setName("java")
                            .setMemberNum(1303904)
                            .setThreadNum(420141)
                            .setPostNum(12862522)
                            .setAvatar("//example.com/forum.jpg"),
                    ).setPage(
                        FrsPageInfoLite
                            .newBuilder()
                            .setCurrentPage(1)
                            .setHasMore(1),
                    ).addUserList(
                        UserLite
                            .newBuilder()
                            .setId(154453178)
                            .setName("tieba_user")
                            .setNameShow("吧吧惊喜官")
                            .setPortrait("tb.1.author"),
                    ).setPageData(
                        FrsPageDataLite
                            .newBuilder()
                            .addFeedList(
                                FeedLayoutLite
                                    .newBuilder()
                                    .setLayout("feed")
                                    .setFeed(feed),
                            ),
                    ),
            ).build()

    private fun buildFeed(
        title: FeedTextGroupLite = textGroup("「AI 点亮你的超能力」创作大赛"),
        includeVideo: Boolean = false,
    ): FeedLite {
        val builder =
            FeedLite
                .newBuilder()
                .setSchema("tiebaapp://router/portal?params=%7B%22pageParams%22%3A%7B%22tid%22%3A10751626484%7D%7D")
                .addComponents(
                    FeedComponentLite
                        .newBuilder()
                        .setComponent("feed_head")
                        .setFeedHead(
                            FeedHeadLite
                                .newBuilder()
                                .setImageData(
                                    FeedHeadImageLite
                                        .newBuilder()
                                        .setImgUrl(AUTHOR_AVATAR_URL),
                                ).addMainData(
                                    FeedHeadSymbolLite
                                        .newBuilder()
                                        .setText(
                                            FeedHeadTextLite
                                                .newBuilder()
                                                .setText("吧吧惊喜官"),
                                        ),
                                ),
                        ),
                ).addComponents(
                    FeedComponentLite
                        .newBuilder()
                        .setComponent("feed_title")
                        .setFeedTitle(title),
                ).addComponents(
                    FeedComponentLite
                        .newBuilder()
                        .setComponent("feed_abstract")
                        .setFeedAbstract(textGroup("2026年被称为智能体深度共生的元年")),
                ).addComponents(
                    FeedComponentLite
                        .newBuilder()
                        .setComponent("feed_pic")
                        .setFeedPic(
                            FeedPicGroupLite
                                .newBuilder()
                                .addPics(
                                    FeedPicLite
                                        .newBuilder()
                                        .setSmallPicUrl("//example.com/small.jpg")
                                        .setBigPicUrl("//example.com/big.jpg")
                                        .setOriginPicUrl("//example.com/origin.jpg")
                                        .setWidth(560)
                                        .setHeight(337),
                                ),
                        ),
                ).addComponents(
                    FeedComponentLite
                        .newBuilder()
                        .setComponent("feed_social")
                        .setFeedSocial(
                            FeedSocialLite
                                .newBuilder()
                                .setTid(10751626484)
                                .setCommentNum(48)
                                .setShareNum(20)
                                .setAgree(
                                    FeedAgreeLite
                                        .newBuilder()
                                        .setAgreeNum(109),
                                ),
                        ),
                ).addBusinessInfo(keyValue("thread_id", "10751626484"))
                .addBusinessInfo(keyValue("user_id", "154453178"))
                .addBusinessInfo(keyValue("create_time", "1779965093"))
                .addBusinessInfo(keyValue("forum_name", "java"))
                .addBusinessInfo(keyValue("forum_avatar", "//example.com/forum.jpg"))

        if (includeVideo) {
            builder.addComponents(
                FeedComponentLite
                    .newBuilder()
                    .setComponent("feed_video")
                    .setFeedVideo(
                        FeedVideoLite
                            .newBuilder()
                            .setVideoInfo(
                                FeedVideoInfoLite
                                    .newBuilder()
                                    .setUrl("//example.com/video.mp4")
                                    .setDuration(51)
                                    .setWidth(960)
                                    .setHeight(720)
                                    .setThumbnail(
                                        FeedThumbnailLite
                                            .newBuilder()
                                            .setUrl("//example.com/thumb.jpg"),
                                    ),
                            ),
                    ),
            )
        }
        return builder.build()
    }

    private fun textGroup(text: String): FeedTextGroupLite =
        FeedTextGroupLite
            .newBuilder()
            .addData(
                FeedTextResourceLite
                    .newBuilder()
                    .setTextInfo(
                        FeedTextLite
                            .newBuilder()
                            .setText(text),
                    ),
            ).build()

    private fun richTextGroup(): FeedTextGroupLite =
        FeedTextGroupLite
            .newBuilder()
            .addData(
                FeedTextResourceLite
                    .newBuilder()
                    .setType(1)
                    .setTextInfo(
                        FeedTextLite
                            .newBuilder()
                            .setText("标题"),
                    ),
            ).addData(
                FeedTextResourceLite
                    .newBuilder()
                    .setType(3)
                    .setEmojiInfo(
                        FeedEmojiLite
                            .newBuilder()
                            .setName("image_emoticon9")
                            .setC("#(泪)"),
                    ),
            ).addData(
                FeedTextResourceLite
                    .newBuilder()
                    .setType(1)
                    .setTextInfo(
                        FeedTextLite
                            .newBuilder()
                            .setText("后缀"),
                    ),
            ).build()

    private fun keyValue(
        key: String,
        value: String,
    ): FeedKeyValueLite =
        FeedKeyValueLite
            .newBuilder()
            .setKey(key)
            .setValue(value)
            .build()
}

private const val AUTHOR_AVATAR_URL = "http://tb.himg.baidu.com/sys/portrait/item/tb.1.author"
