package app.tiebalite.core.network.source.tbclient

import app.tiebalite.core.network.client.NetworkDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TbClientRequestTest {
    @Test
    fun commonReqFieldsMatchTbclientLabOrderAndValues() {
        val fields =
            commonReqFields(
                identity = TestTbClientIdentity,
                device = TestTbClientDevice,
                screen = TestTbClientScreen,
                timestamp = TestTbClientTimestamp,
                from = "tieba",
                qType = 2,
                bduss = "BDUSS",
                stoken = "STOKEN",
                includeApplist = true,
            )

        assertEquals(
            listOf(
                10, 3, 1, 2, 25, 5, 8, 49, 54, 29, 26, 35, 55, 7, 32, 33, 70, 53,
                61, 50, 43, 6, 41, 51, 28, 59, 9, 44, 12, 34, 88, 63, 24, 40, 36,
                39, 38, 37, 42, 56, 57, 30, 87, 62,
            ),
            fields.map { field -> field.number },
        )
        assertEquals(ProtoWire.string(10, "BDUSS"), fields[0])
        assertEquals(ProtoWire.string(3, "wappc_1_2"), fields[1])
        assertEquals(ProtoWire.varint(1, 2), fields[2])
        assertEquals(ProtoWire.string(2, NetworkDefaults.TBCLIENT_CLIENT_VERSION), fields[3])
        assertEquals(ProtoWire.string(25, "15"), fields[4])
        assertEquals(ProtoWire.string(29, ""), fields[9])
        assertEquals(ProtoWire.string(35, "c3aid"), fields[11])
        assertEquals(ProtoWire.string(7, "cuid"), fields[13])
        assertEquals(ProtoWire.string(32, "cuid"), fields[14])
        assertEquals(ProtoWire.string(53, "19700101"), fields[17])
        assertEquals(ProtoWire.string(6, "tieba"), fields[21])
        assertEquals(ProtoWire.string(9, "Android"), fields[26])
        assertEquals(ProtoWire.varint(40, 2), fields[33])
        assertEquals(ProtoWire.double(39, 3.0), fields[35])
        assertEquals(ProtoWire.varint(38, 2400), fields[36])
        assertEquals(ProtoWire.varint(37, 1080), fields[37])
        assertEquals(ProtoWire.string(30, "STOKEN"), fields[41])
        assertEquals(ProtoWire.string(62, NetworkDefaults.TBCLIENT_USER_AGENT), fields[43])
        assertFalse(fields.any { field -> field.number == 11 })
    }

    @Test
    fun commonReqFieldsOmitOptionalAuthAndApplistFields() {
        val fields =
            commonReqFields(
                identity = TestTbClientIdentity,
                device = TestTbClientDevice,
                screen = TestTbClientScreen,
                timestamp = TestTbClientTimestamp,
                from = "tieba",
                qType = 0,
                bduss = null,
                stoken = null,
                includeApplist = false,
            )

        assertFalse(fields.any { field -> field.number == 10 })
        assertFalse(fields.any { field -> field.number == 29 })
        assertFalse(fields.any { field -> field.number == 30 })
        assertFalse(fields.any { field -> field.number == 11 })
        assertEquals(ProtoWire.string(3, "wappc_1_2"), fields.first { field -> field.number == 3 })
        assertEquals(ProtoWire.varint(40, 0), fields.first { field -> field.number == 40 })
    }
}
