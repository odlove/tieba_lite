package app.tiebalite.core.network.source.tbclient

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object ProtoWire {
    private const val WireVarint = 0
    private const val WireFixed64 = 1
    private const val WireLengthDelimited = 2

    sealed interface Field {
        val number: Int

        fun writeTo(output: ByteArrayOutputStream)
    }

    data class Varint(
        override val number: Int,
        val value: Long,
    ) : Field {
        override fun writeTo(output: ByteArrayOutputStream) {
            output.writeVarint(key(number, WireVarint))
            output.writeVarint(value)
        }
    }

    data class Fixed64(
        override val number: Int,
        val value: Double,
    ) : Field {
        override fun writeTo(output: ByteArrayOutputStream) {
            output.writeVarint(key(number, WireFixed64))
            output.write(
                ByteBuffer
                    .allocate(Long.SIZE_BYTES)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putDouble(value)
                    .array(),
            )
        }
    }

    data class LengthDelimited(
        override val number: Int,
        val value: ByteArray,
    ) : Field {
        override fun writeTo(output: ByteArrayOutputStream) {
            output.writeVarint(key(number, WireLengthDelimited))
            output.writeVarint(value.size.toLong())
            output.write(value)
        }

        override fun equals(other: Any?): Boolean =
            this === other ||
                other is LengthDelimited &&
                number == other.number &&
                value.contentEquals(other.value)

        override fun hashCode(): Int = 31 * number + value.contentHashCode()
    }

    fun varint(
        number: Int,
        value: Int,
    ): Field = Varint(number, value.toLong())

    fun varint(
        number: Int,
        value: Long,
    ): Field = Varint(number, value)

    fun bool(
        number: Int,
        value: Boolean,
    ): Field = Varint(number, if (value) 1 else 0)

    fun double(
        number: Int,
        value: Double,
    ): Field = Fixed64(number, value)

    fun string(
        number: Int,
        value: String,
    ): Field = LengthDelimited(number, value.toByteArray())

    fun bytes(
        number: Int,
        value: ByteArray,
    ): Field = LengthDelimited(number, value)

    fun message(
        number: Int,
        fields: List<Field>,
    ): Field = LengthDelimited(number, encode(fields))

    fun encode(fields: List<Field>): ByteArray =
        ByteArrayOutputStream().use { output ->
            fields.forEach { field ->
                field.writeTo(output)
            }
            output.toByteArray()
        }

    private fun key(
        number: Int,
        wireType: Int,
    ): Long = ((number shl 3) or wireType).toLong()

    private fun ByteArrayOutputStream.writeVarint(value: Long) {
        var remaining = value
        while ((remaining and -0x80L) != 0L) {
            write(((remaining and 0x7fL) or 0x80L).toInt())
            remaining = remaining ushr 7
        }
        write(remaining.toInt())
    }
}
