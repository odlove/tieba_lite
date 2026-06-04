package app.tiebalite.core.network.source.tbclient

import app.tiebalite.core.network.source.tbclient.auth.TbClientLoginApi
import app.tiebalite.core.network.source.tbclient.auth.TbClientLoginNetworkSource
import app.tiebalite.core.network.source.tbclient.auth.TbClientProfileApi
import app.tiebalite.core.network.source.tbclient.auth.TbClientProfileNetworkSource
import app.tiebalite.core.network.source.tbclient.forum.ForumGuideApi
import app.tiebalite.core.network.source.tbclient.forum.ForumGuideNetworkSource
import app.tiebalite.core.network.source.tbclient.forum.FrsPageApi
import app.tiebalite.core.network.source.tbclient.forum.FrsPageNetworkSource
import app.tiebalite.core.network.source.tbclient.thread.PbFloorApi
import app.tiebalite.core.network.source.tbclient.thread.PbFloorNetworkSource
import app.tiebalite.core.network.source.tbclient.thread.PbPageApi
import app.tiebalite.core.network.source.tbclient.thread.PbPageNetworkSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TbClientNetworkSourceValidationTest {
    @Test
    fun loginRejectsBlankBdussBeforeRequest() {
        val (api, capture) = captureApi<TbClientLoginApi>()

        val result =
            runSuspend {
                TbClientLoginNetworkSource(api).login(
                    bduss = "",
                    stoken = "STOKEN",
                )
            }

        assertTrue(result.isFailure)
        assertFalse(capture.hasRequest)
    }

    @Test
    fun profileRejectsNonPositiveUserIdBeforeRequest() {
        val (api, capture) = captureApi<TbClientProfileApi>()

        val result =
            runSuspend {
                TbClientProfileNetworkSource(
                    api = api,
                    device = TestTbClientDevice,
                    identity = TestTbClientIdentity,
                ).fetchProfile(userId = 0L)
            }

        assertTrue(result.isFailure)
        assertFalse(capture.hasRequest)
    }

    @Test
    fun forumGuideRejectsBlankAuthBeforeRequest() {
        val (api, blankBdussCapture) = captureApi<ForumGuideApi>()
        val blankBdussResult =
            runSuspend {
                ForumGuideNetworkSource(
                    api = api,
                    device = TestTbClientDevice,
                    identity = TestTbClientIdentity,
                ).fetchForumGuide(
                    bduss = "",
                    stoken = "STOKEN",
                )
            }

        assertTrue(blankBdussResult.isFailure)
        assertFalse(blankBdussCapture.hasRequest)

        val (blankStokenApi, blankStokenCapture) = captureApi<ForumGuideApi>()
        val blankStokenResult =
            runSuspend {
                ForumGuideNetworkSource(
                    api = blankStokenApi,
                    device = TestTbClientDevice,
                    identity = TestTbClientIdentity,
                ).fetchForumGuide(
                    bduss = "BDUSS",
                    stoken = "",
                )
            }

        assertTrue(blankStokenResult.isFailure)
        assertFalse(blankStokenCapture.hasRequest)
    }

    @Test
    fun frsPageRejectsBlankForumNameBeforeRequest() {
        val (api, capture) = captureApi<FrsPageApi>()

        val result =
            runSuspend {
                FrsPageNetworkSource(
                    api = api,
                    device = TestTbClientDevice,
                    identity = TestTbClientIdentity,
                ).fetchPage(forumName = "")
            }

        assertTrue(result.isFailure)
        assertFalse(capture.hasRequest)
    }

    @Test
    fun pbPageRejectsNonPositiveThreadIdBeforeRequest() {
        val (api, capture) = captureApi<PbPageApi>()

        val result =
            runSuspend {
                PbPageNetworkSource(
                    api = api,
                    device = TestTbClientDevice,
                    identity = TestTbClientIdentity,
                ).fetchPage(threadId = 0L)
            }

        assertTrue(result.isFailure)
        assertFalse(capture.hasRequest)
    }

    @Test
    fun pbFloorRejectsNonPositiveIdsBeforeRequest() {
        val (blankThreadApi, blankThreadCapture) = captureApi<PbFloorApi>()
        val blankThreadResult =
            runSuspend {
                PbFloorNetworkSource(
                    api = blankThreadApi,
                    device = TestTbClientDevice,
                    identity = TestTbClientIdentity,
                ).fetchFloor(
                    threadId = 0L,
                    postId = 456L,
                )
            }

        assertTrue(blankThreadResult.isFailure)
        assertFalse(blankThreadCapture.hasRequest)

        val (blankPostApi, blankPostCapture) = captureApi<PbFloorApi>()
        val blankPostResult =
            runSuspend {
                PbFloorNetworkSource(
                    api = blankPostApi,
                    device = TestTbClientDevice,
                    identity = TestTbClientIdentity,
                ).fetchFloor(
                    threadId = 123L,
                    postId = 0L,
                )
            }

        assertTrue(blankPostResult.isFailure)
        assertFalse(blankPostCapture.hasRequest)
    }
}

private inline fun <reified T> captureApi(): Pair<T, CapturingInterceptor> {
    val capture = CapturingInterceptor()
    return retrofitForCapture(capture).create(T::class.java) to capture
}
