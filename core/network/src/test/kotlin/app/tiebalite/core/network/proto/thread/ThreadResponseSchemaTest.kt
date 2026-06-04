package app.tiebalite.core.network.proto.thread

import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadResponseSchemaTest {
    @Test
    fun threadAgreeParsesAsMessage() {
        val bytes =
            ThreadInfoLite
                .newBuilder()
                .setAgree(
                    AgreeLite
                        .newBuilder()
                        .setAgreeNum(11)
                        .build(),
                ).build()
                .toByteArray()

        val thread = ThreadInfoLite.parseFrom(bytes)

        assertEquals(11, thread.agree.agreeNum)
    }

    @Test
    fun postAgreeParsesAsMessage() {
        val bytes =
            ThreadPostLite
                .newBuilder()
                .setAgree(
                    AgreeLite
                        .newBuilder()
                        .setAgreeNum(42)
                        .build(),
                ).build()
                .toByteArray()

        val post = ThreadPostLite.parseFrom(bytes)

        assertEquals(42, post.agree.agreeNum)
    }

    @Test
    fun subPostAgreeParsesAsMessage() {
        val bytes =
            ThreadSubPostListLite
                .newBuilder()
                .setAgree(
                    AgreeLite
                        .newBuilder()
                        .setAgreeNum(7)
                        .build(),
                ).build()
                .toByteArray()

        val subPost = ThreadSubPostListLite.parseFrom(bytes)

        assertEquals(7, subPost.agree.agreeNum)
    }
}
