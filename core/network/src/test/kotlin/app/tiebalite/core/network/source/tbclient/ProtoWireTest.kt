package app.tiebalite.core.network.source.tbclient

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ProtoWireTest {
    @Test
    fun encodeKeepsExplicitDefaultFields() {
        val bytes =
            ProtoWire.encode(
                listOf(
                    ProtoWire.varint(1, 0),
                    ProtoWire.string(2, ""),
                    ProtoWire.message(3, listOf(ProtoWire.varint(1, 0))),
                ),
            )

        assertArrayEquals(
            byteArrayOf(
                0x08,
                0x00,
                0x12,
                0x00,
                0x1a,
                0x02,
                0x08,
                0x00,
            ),
            bytes,
        )
    }
}
