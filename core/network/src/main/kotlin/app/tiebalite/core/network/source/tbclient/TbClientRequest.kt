package app.tiebalite.core.network.source.tbclient

import android.content.res.Resources
import app.tiebalite.core.network.client.TbClientDevice
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientIdentity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

internal data class TbClientScreen(
    val width: Int,
    val height: Int,
    val density: Double,
) {
    companion object {
        fun current(): TbClientScreen {
            val metrics = Resources.getSystem().displayMetrics
            return TbClientScreen(
                width = metrics.widthPixels.takeIf { it > 0 } ?: DefaultScreenWidth,
                height = metrics.heightPixels.takeIf { it > 0 } ?: DefaultScreenHeight,
                density = metrics.density.takeIf { it > 0f }?.toDouble() ?: DefaultScreenDensity,
            )
        }
    }
}

internal fun buildTbClientCookie(
    identity: TbClientIdentity,
    device: TbClientDevice,
): String = "ka=open;CUID=${identity.cuid};TBBRAND=${device.model};"

internal fun buildDataPart(data: ByteArray): MultipartBody.Part =
    MultipartBody.Part.createFormData(
        "data",
        "file",
        data.toRequestBody(TbClientBinaryMediaType),
    )

internal fun buildTextParts(parts: Map<String, String>): Map<String, RequestBody> =
    linkedMapOf<String, RequestBody>().apply {
        parts.forEach { (name, value) ->
            if (value.isNotBlank()) {
                put(name, value.toRequestBody(TbClientPlainTextMediaType))
            }
        }
    }

internal fun stokenParts(stoken: String?): Map<String, String> =
    if (stoken.isNullOrBlank()) {
        emptyMap()
    } else {
        mapOf("stoken" to stoken)
    }

internal fun commonReqFields(
    identity: TbClientIdentity,
    device: TbClientDevice,
    screen: TbClientScreen,
    timestamp: Long,
    from: String,
    qType: Int,
    bduss: String?,
    stoken: String?,
    includeApplist: Boolean,
): List<ProtoWire.Field> =
    buildList {
        bduss?.takeIf { it.isNotBlank() }?.let {
            add(ProtoWire.string(10, it))
        }
        add(ProtoWire.string(3, identity.clientId))
        add(ProtoWire.varint(1, 2))
        add(ProtoWire.string(2, NetworkDefaults.TBCLIENT_CLIENT_VERSION))
        add(ProtoWire.string(25, device.osVersion))
        add(ProtoWire.string(5, ""))
        add(ProtoWire.varint(8, timestamp))
        add(ProtoWire.varint(49, 0L))
        add(ProtoWire.string(54, ""))
        if (includeApplist) {
            add(ProtoWire.string(29, ""))
        }
        add(ProtoWire.string(26, device.brand))
        add(ProtoWire.string(35, identity.c3Aid))
        add(ProtoWire.varint(55, 1))
        add(ProtoWire.string(7, identity.cuid))
        add(ProtoWire.string(32, identity.cuidGalaxy2))
        add(ProtoWire.string(33, ""))
        add(ProtoWire.string(70, ""))
        add(ProtoWire.string(53, eventDay(timestamp)))
        add(ProtoWire.string(61, ""))
        add(ProtoWire.varint(50, 0L))
        add(ProtoWire.string(43, ""))
        add(ProtoWire.string(6, from))
        add(ProtoWire.varint(41, 0))
        add(ProtoWire.varint(51, 0L))
        add(ProtoWire.string(28, ""))
        add(ProtoWire.string(59, ""))
        add(ProtoWire.string(9, device.model))
        add(ProtoWire.string(44, ""))
        add(ProtoWire.varint(12, 1))
        add(ProtoWire.string(34, ""))
        add(ProtoWire.string(88, ""))
        add(ProtoWire.varint(63, 1))
        add(ProtoWire.string(24, "1.0.3"))
        add(ProtoWire.varint(40, qType))
        add(ProtoWire.string(36, ""))
        add(ProtoWire.double(39, screen.density))
        add(ProtoWire.varint(38, screen.height))
        add(ProtoWire.varint(37, screen.width))
        add(ProtoWire.string(42, ""))
        add(ProtoWire.string(56, ""))
        add(ProtoWire.varint(57, 0))
        stoken?.takeIf { it.isNotBlank() }?.let {
            add(ProtoWire.string(30, it))
        }
        add(ProtoWire.string(87, ""))
        add(ProtoWire.string(62, NetworkDefaults.TBCLIENT_USER_AGENT))
    }

internal fun appPosFields(): List<ProtoWire.Field> =
    listOf(
        ProtoWire.varint(6, 0L),
        ProtoWire.bool(2, true),
        ProtoWire.string(1, "02:00:00:00:00:00"),
        ProtoWire.string(7, ""),
        ProtoWire.string(3, "BD09LL"),
    )

private fun eventDay(timestamp: Long): String =
    SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(timestamp))

private val TbClientPlainTextMediaType = "text/plain".toMediaType()
private val TbClientBinaryMediaType = "application/octet-stream".toMediaType()

private const val DefaultScreenWidth = 1080
private const val DefaultScreenHeight = 2400
private const val DefaultScreenDensity = 3.0
