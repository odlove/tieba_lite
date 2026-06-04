package app.tiebalite.core.network.source.tbclient

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class DecodedProtoField(
    val number: Int,
    val wireType: Int,
    val varint: Long? = null,
    val fixed64: Double? = null,
    val bytes: ByteArray? = null,
) {
    fun decodeMessage(): List<DecodedProtoField> = decodeProtoFields(requireNotNull(bytes))

    fun signedVarint32(): Int = requireNotNull(varint).toInt()

    fun stringValue(): String = requireNotNull(bytes).decodeToString()
}

fun decodeProtoFields(bytes: ByteArray): List<DecodedProtoField> {
    val fields = mutableListOf<DecodedProtoField>()
    var index = 0
    while (index < bytes.size) {
        val key = readVarint(bytes = bytes, startIndex = index)
        index = key.nextIndex
        val number = (key.value ushr 3).toInt()
        val wireType = (key.value and 0x07).toInt()
        when (wireType) {
            0 -> {
                val value = readVarint(bytes = bytes, startIndex = index)
                fields += DecodedProtoField(number = number, wireType = wireType, varint = value.value)
                index = value.nextIndex
            }
            1 -> {
                val value =
                    ByteBuffer
                        .wrap(bytes, index, Long.SIZE_BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .double
                fields += DecodedProtoField(number = number, wireType = wireType, fixed64 = value)
                index += Long.SIZE_BYTES
            }
            2 -> {
                val length = readVarint(bytes = bytes, startIndex = index)
                index = length.nextIndex
                val endIndex = index + length.value.toInt()
                fields += DecodedProtoField(number = number, wireType = wireType, bytes = bytes.copyOfRange(index, endIndex))
                index = endIndex
            }
            else -> error("unsupported protobuf wire type: $wireType")
        }
    }
    return fields
}

fun List<DecodedProtoField>.firstField(number: Int): DecodedProtoField = first { field -> field.number == number }

fun List<DecodedProtoField>.fieldNumbers(): List<Int> = map { field -> field.number }

private data class VarintRead(
    val value: Long,
    val nextIndex: Int,
)

private fun readVarint(
    bytes: ByteArray,
    startIndex: Int,
): VarintRead {
    var value = 0L
    var shift = 0
    var index = startIndex
    while (index < bytes.size) {
        val current = bytes[index].toLong() and 0xff
        value = value or ((current and 0x7f) shl shift)
        index += 1
        if ((current and 0x80) == 0L) {
            return VarintRead(value = value, nextIndex = index)
        }
        shift += 7
    }
    error("truncated protobuf varint")
}
