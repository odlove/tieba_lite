package app.tiebalite.core.network.source.tbclient.thread

import android.content.res.Resources
import android.os.Build
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.proto.thread.PbFloorRequestDataLite
import app.tiebalite.core.network.proto.thread.PbFloorRequestLite
import app.tiebalite.core.network.proto.thread.PbFloorResponseLite
import app.tiebalite.core.network.proto.thread.ThreadCommonReqLite
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class PbFloorNetworkSource(
    private val api: PbFloorApi,
) {
    private val identity = FloorDeviceIdentity.create()

    suspend fun fetchFloor(
        threadId: Long,
        postId: Long,
        page: Int = 1,
        subPostId: Long = 0L,
        forumId: Long = 0L,
        cmd: Int = NetworkDefaults.PB_FLOOR_CMD,
        clientUserToken: String? = null,
        bduss: String? = null,
        stoken: String? = null,
        tbs: String? = null,
    ): Result<PbFloorRaw> {
        return try {
            val requestBytes =
                buildRequestBody(
                    threadId = threadId,
                    postId = postId,
                    page = page,
                    subPostId = subPostId,
                    forumId = forumId,
                    bduss = bduss,
                    stoken = stoken,
                    tbs = tbs,
                )
            val formParts = buildFormParts(stoken)
            val dataPart =
                MultipartBody.Part.createFormData(
                    "data",
                    "file",
                    requestBytes.toRequestBody(BinaryMediaType),
                )

            val responseBytes =
                api.getPbFloor(
                    cmd = cmd,
                    clientUserToken = clientUserToken,
                    cookie = buildCookie(identity.cuid),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    formParts = formParts,
                    data = dataPart,
                ).bytes()
            val response = PbFloorResponseLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("pb floor api failed: $errorNo $errorMessage")
            }
            Result.success(
                PbFloorRaw(
                    body = responseBytes,
                    response = response,
                ),
            )
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }

    private fun buildFormParts(
        stoken: String?,
    ): Map<String, RequestBody> {
        val parts = linkedMapOf<String, RequestBody>()
        stoken?.takeIf { it.isNotBlank() }?.let {
            parts["stoken"] = it.toRequestBody(PlainTextMediaType)
        }
        return parts
    }

    private fun buildRequestBody(
        threadId: Long,
        postId: Long,
        page: Int,
        subPostId: Long,
        forumId: Long,
        bduss: String?,
        stoken: String?,
        tbs: String?,
    ): ByteArray {
        val metrics = Resources.getSystem().displayMetrics
        val scrW = metrics.widthPixels.takeIf { it > 0 } ?: DefaultScrW
        val scrH = metrics.heightPixels.takeIf { it > 0 } ?: DefaultScrH
        val scrDip = metrics.density.takeIf { it > 0f }?.toDouble() ?: DefaultScrDip

        val common =
            ThreadCommonReqLite.newBuilder()
                .setClientType(2)
                .setClientVersion(NetworkDefaults.TBCLIENT_CLIENT_VERSION)
                .setClientId(identity.clientId)
                .setPhoneImei("")
                .setFrom(From)
                .setCuid(identity.cuid)
                .setTimestamp(System.currentTimeMillis())
                .setModel(Build.MODEL)
                .setBduss(bduss.orEmpty())
                .setTbs(tbs.orEmpty())
                .setNetType(1)
                .setPhoneNewimei("")
                .setKa("open")
                .setStoken(stoken.orEmpty())
                .setCuidGalaxy2(identity.cuidGalaxy2)
                .setCuidGid("")
                .setOaid("")
                .setC3Aid(identity.c3Aid)
                .setScrW(scrW)
                .setScrH(scrH)
                .setScrDip(scrDip)
                .setQType(0)
                .setPersonalizedRecSwitch(1)
                .build()

        return PbFloorRequestLite.newBuilder()
            .setData(
                PbFloorRequestDataLite.newBuilder()
                    .setKz(threadId)
                    .setPid(postId)
                    .setSpid(subPostId)
                    .setPn(page.coerceAtLeast(1))
                    .setScrW(scrW)
                    .setScrH(scrH)
                    .setScrDip(scrDip)
                    .setStType("")
                    .setCommon(common)
                    .setIsCommReverse(0)
                    .setForumId(forumId)
                    .setOriUgcType(0)
                    .build(),
            ).build()
            .toByteArray()
    }

    private fun buildCookie(cuid: String): String = "ka=open;CUID=$cuid;TBBRAND=${Build.MODEL};"
}

data class PbFloorRaw(
    val body: ByteArray,
    val response: PbFloorResponseLite,
)

private val PlainTextMediaType = "text/plain".toMediaType()
private val BinaryMediaType = "application/octet-stream".toMediaType()

private const val From = "1020031h"
private const val DefaultScrW = 1080
private const val DefaultScrH = 2400
private const val DefaultScrDip = 3.0

private data class FloorDeviceIdentity(
    val clientId: String,
    val cuid: String,
    val cuidGalaxy2: String,
    val c3Aid: String,
) {
    companion object {
        fun create(): FloorDeviceIdentity {
            val initTime = System.currentTimeMillis()
            val clientId = "wappc_${initTime}_${(Math.random() * 1000).roundToInt()}"
            val cuid = java.util.UUID.randomUUID().toString().replace("-", "")
            val c3Aid = java.util.UUID.randomUUID().toString().replace("-", "")
            return FloorDeviceIdentity(
                clientId = clientId,
                cuid = cuid,
                cuidGalaxy2 = cuid,
                c3Aid = c3Aid,
            )
        }
    }
}
