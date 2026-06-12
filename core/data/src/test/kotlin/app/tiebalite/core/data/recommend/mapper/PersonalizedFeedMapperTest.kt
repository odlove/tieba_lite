package app.tiebalite.core.data.recommend.mapper

import app.tiebalite.core.network.proto.feed.FeedAgreeLite
import app.tiebalite.core.network.proto.feed.FeedComponentLite
import app.tiebalite.core.network.proto.feed.FeedHeadButtonLite
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
import app.tiebalite.core.network.proto.recommend.PersonalizedPageDataLite
import app.tiebalite.core.network.proto.recommend.PersonalizedResponseDataLite
import app.tiebalite.core.network.proto.recommend.PersonalizedResponseLite
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalizedFeedMapperTest {
    private val mapper = PersonalizedFeedMapper()

    @Test
    fun mapReadsItemsFromPageDataFeedList() {
        val item = mapper.map(rawWithFeed(buildFeed())).single()

        assertEquals("10781732851", item.id)
        assertEquals("潜伏攻击！哦？痛吗？", item.titleText)
        assertEquals("minecraft", item.forumName)
        assertEquals("https://example.com/forum.jpg", item.forumAvatarUrl)
        assertEquals("Garam1314", item.authorName)
        assertEquals(AUTHOR_AVATAR_URL, item.authorAvatarUrl)
        assertEquals("2026年被称为智能体深度共生的元年", item.snippetText)
        assertEquals(12, item.replyCount)
        assertEquals(34, item.agreeCount)
        assertEquals(5L, item.shareCount)
        assertEquals(1781075943L, item.lastTimeTimestampSeconds)
        assertEquals("https://example.com/origin.jpg", item.images.single().url)
        assertEquals(560, item.images.single().width)
        assertEquals(337, item.images.single().height)
    }

    @Test
    fun mapReadsVideoFromFeedVideoComponent() {
        val item = mapper.map(rawWithFeed(buildFeed(includeVideo = true))).single()
        val video = item.video ?: error("video missing")

        assertEquals("https://example.com/video.mp4", video.url)
        assertEquals("https://example.com/thumb.jpg", video.coverUrl)
        assertEquals(1152, video.width)
        assertEquals(720, video.height)
        assertEquals(66, video.durationSeconds)
    }

    @Test
    fun mapReturnsEmptyWhenPageDataIsMissing() {
        val raw =
            PersonalizedFeedRaw(
                body = ByteArray(0),
                response =
                    PersonalizedResponseLite
                        .newBuilder()
                        .setData(
                            PersonalizedResponseDataLite.newBuilder(),
                        ).build(),
            )

        assertTrue(mapper.map(raw).isEmpty())
    }

    private fun rawWithFeed(feed: FeedLite): PersonalizedFeedRaw =
        PersonalizedFeedRaw(
            body = ByteArray(0),
            response =
                PersonalizedResponseLite
                    .newBuilder()
                    .setData(
                        PersonalizedResponseDataLite
                            .newBuilder()
                            .setPageData(
                                PersonalizedPageDataLite
                                    .newBuilder()
                                    .addFeedList(
                                        FeedLayoutLite
                                            .newBuilder()
                                            .setLayout("feed")
                                            .setFeed(feed),
                                    ),
                            ),
                    ).build(),
        )

    private fun buildFeed(includeVideo: Boolean = false): FeedLite {
        val builder =
            FeedLite
                .newBuilder()
                .setSchema("tiebaapp://router/portal?params=%7B%22pageParams%22%3A%7B%22tid%22%3A10781732851%7D%7D")
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
                                                .setText("Garam1314"),
                                        ),
                                ).setButton(
                                    FeedHeadButtonLite
                                        .newBuilder()
                                        .addBusinessInfo(keyValue("user_id", "3460690406")),
                                ),
                        ),
                ).addComponents(
                    FeedComponentLite
                        .newBuilder()
                        .setComponent("feed_title")
                        .setFeedTitle(textGroup("潜伏攻击！哦？痛吗？")),
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
                                .setTid(10781732851)
                                .setCommentNum(12)
                                .setShareNum(5)
                                .setAgree(
                                    FeedAgreeLite
                                        .newBuilder()
                                        .setAgreeNum(34),
                                ),
                        ),
                ).addBusinessInfo(keyValue("forum_name", "minecraft"))
                .addBusinessInfo(keyValue("forum_avatar", "//example.com/forum.jpg"))
                .addBusinessInfo(keyValue("thread_id", "10781732851"))
                .addBusinessInfo(keyValue("create_time", "1781075943"))

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
                                    .setDuration(66)
                                    .setWidth(1152)
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
                    .setType(1)
                    .setTextInfo(
                        FeedTextLite
                            .newBuilder()
                            .setText(text),
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
