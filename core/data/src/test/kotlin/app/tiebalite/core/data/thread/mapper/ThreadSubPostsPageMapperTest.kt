package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.network.proto.thread.PbFloorResponseDataLite
import app.tiebalite.core.network.proto.thread.PbFloorResponseLite
import app.tiebalite.core.network.proto.thread.ThreadPageInfoLite
import app.tiebalite.core.network.source.tbclient.thread.PbFloorRaw
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreadSubPostsPageMapperTest {
    private val mapper = ThreadSubPostsPageMapper()

    @Test
    fun mapUsesExplicitHasMoreWhenTotalPageLooksExhausted() {
        val page =
            mapper.map(
                raw(
                    ThreadPageInfoLite
                        .newBuilder()
                        .setCurrentPage(2)
                        .setTotalPage(2)
                        .setHasMore(1),
                ),
            )

        assertTrue(page.hasMore)
    }

    @Test
    fun mapUsesExplicitHasMoreWhenTotalPageLooksAvailable() {
        val page =
            mapper.map(
                raw(
                    ThreadPageInfoLite
                        .newBuilder()
                        .setCurrentPage(1)
                        .setTotalPage(2)
                        .setHasMore(0),
                ),
            )

        assertFalse(page.hasMore)
    }

    private fun raw(page: ThreadPageInfoLite.Builder): PbFloorRaw {
        val response =
            PbFloorResponseLite
                .newBuilder()
                .setData(
                    PbFloorResponseDataLite
                        .newBuilder()
                        .setPage(page),
                ).build()
        return PbFloorRaw(
            body = response.toByteArray(),
            response = response,
        )
    }
}
