package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.network.proto.thread.PbListResponseDataLite
import app.tiebalite.core.network.proto.thread.PbListResponseLite
import app.tiebalite.core.network.proto.thread.ThreadPageInfoLite
import app.tiebalite.core.network.proto.thread.ThreadPostLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite
import app.tiebalite.core.network.source.tbclient.thread.PbPageRaw
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThreadPageMapperTest {
    private val mapper = ThreadPageMapper()

    @Test
    fun mapDoesNotTreatStandaloneFirstFloorAsReplyListEnd() {
        val response =
            PbListResponseLite
                .newBuilder()
                .setData(
                    PbListResponseDataLite
                        .newBuilder()
                        .setFirstFloor(post(id = 100L, floor = 1))
                        .addPostList(post(id = 200L, floor = 20))
                        .setPage(
                            ThreadPageInfoLite
                                .newBuilder()
                                .setCurrentPage(1)
                                .setTotalPage(2)
                                .setHasMore(1),
                        ),
                ).build()

        val page =
            mapper.map(
                PbPageRaw(
                    body = response.toByteArray(),
                    response = response,
                ),
            )

        assertNotNull(page.firstFloorPost)
        assertFalse(page.containsFirstFloorPost)
    }

    private fun post(
        id: Long,
        floor: Int,
    ): ThreadPostLite =
        ThreadPostLite
            .newBuilder()
            .setId(id)
            .setFloor(floor)
            .setAuthor(
                ThreadUserLite
                    .newBuilder()
                    .setId(id)
                    .setName("user$id"),
            ).build()
}
