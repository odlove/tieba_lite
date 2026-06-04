package app.tiebalite.core.data.forum.mapper

import app.tiebalite.core.network.proto.frs.FrsAgreeLite
import app.tiebalite.core.network.proto.frs.FrsComponentLite
import app.tiebalite.core.network.proto.frs.FrsFeedLite
import app.tiebalite.core.network.proto.frs.FrsKeyValueLite
import app.tiebalite.core.network.proto.frs.FrsLayoutLite
import app.tiebalite.core.network.proto.frs.FrsPageDataLite
import app.tiebalite.core.network.proto.frs.FrsPageForumInfoLite
import app.tiebalite.core.network.proto.frs.FrsPageInfoLite
import app.tiebalite.core.network.proto.frs.FrsPageResponseLite
import app.tiebalite.core.network.proto.frs.FrsPageResponseDataLite
import app.tiebalite.core.network.proto.frs.FrsPicGroupLite
import app.tiebalite.core.network.proto.frs.FrsPicLite
import app.tiebalite.core.network.proto.frs.FrsSocialLite
import app.tiebalite.core.network.proto.frs.FrsTextGroupLite
import app.tiebalite.core.network.proto.frs.FrsTextLite
import app.tiebalite.core.network.proto.frs.FrsTextResourceLite
import app.tiebalite.core.network.proto.recommend.UserLite
import app.tiebalite.core.network.source.tbclient.forum.FrsPageRaw
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
        assertEquals("「AI 点亮你的超能力」创作大赛", firstItem.title)
        assertFalse(firstItem.snippet.isNullOrBlank())
        assertEquals("吧吧惊喜官", firstItem.authorName)
        assertEquals(48, firstItem.replyCount)
        assertEquals(109, firstItem.agreeCount)
        assertEquals(20L, firstItem.shareCount)
        assertNotNull(firstItem.lastTimeTimestampSeconds)
        assertEquals("https://example.com/origin.jpg", firstItem.images.single().url)
        assertEquals(560, firstItem.images.single().width)
        assertEquals(337, firstItem.images.single().height)
    }

    private fun buildResponse(): FrsPageResponseLite =
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
                                FrsLayoutLite
                                    .newBuilder()
                                    .setLayout("feed")
                                    .setFeed(buildFeed()),
                            ),
                    ),
            ).build()

    private fun buildFeed(): FrsFeedLite =
        FrsFeedLite
            .newBuilder()
            .setSchema("tiebaapp://router/portal?params=%7B%22pageParams%22%3A%7B%22tid%22%3A10751626484%7D%7D")
            .addComponents(
                FrsComponentLite
                    .newBuilder()
                    .setComponent("feed_title")
                    .setFeedTitle(textGroup("「AI 点亮你的超能力」创作大赛")),
            ).addComponents(
                FrsComponentLite
                    .newBuilder()
                    .setComponent("feed_abstract")
                    .setFeedAbstract(textGroup("2026年被称为智能体深度共生的元年")),
            ).addComponents(
                FrsComponentLite
                    .newBuilder()
                    .setComponent("feed_pic")
                    .setFeedPic(
                        FrsPicGroupLite
                            .newBuilder()
                            .addPics(
                                FrsPicLite
                                    .newBuilder()
                                    .setSmallPicUrl("//example.com/small.jpg")
                                    .setBigPicUrl("//example.com/big.jpg")
                                    .setOriginPicUrl("//example.com/origin.jpg")
                                    .setWidth(560)
                                    .setHeight(337),
                            ),
                    ),
            ).addComponents(
                FrsComponentLite
                    .newBuilder()
                    .setComponent("feed_social")
                    .setFeedSocial(
                        FrsSocialLite
                            .newBuilder()
                            .setTid(10751626484)
                            .setCommentNum(48)
                            .setShareNum(20)
                            .setAgree(
                                FrsAgreeLite
                                    .newBuilder()
                                    .setAgreeNum(109),
                            ),
                    ),
            ).addBusinessInfo(keyValue("thread_id", "10751626484"))
            .addBusinessInfo(keyValue("user_id", "154453178"))
            .addBusinessInfo(keyValue("create_time", "1779965093"))
            .build()

    private fun textGroup(text: String): FrsTextGroupLite =
        FrsTextGroupLite
            .newBuilder()
            .addData(
                FrsTextResourceLite
                    .newBuilder()
                    .setTextInfo(
                        FrsTextLite
                            .newBuilder()
                            .setText(text),
                    ),
            ).build()

    private fun keyValue(
        key: String,
        value: String,
    ): FrsKeyValueLite =
        FrsKeyValueLite
            .newBuilder()
            .setKey(key)
            .setValue(value)
            .build()
}
