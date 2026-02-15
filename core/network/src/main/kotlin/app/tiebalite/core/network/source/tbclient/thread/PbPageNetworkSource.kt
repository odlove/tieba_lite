package app.tiebalite.core.network.source.tbclient.thread

import android.content.res.Resources
import android.os.Build
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.proto.thread.PbPageRequestDataLite
import app.tiebalite.core.network.proto.thread.PbPageRequestLite
import app.tiebalite.core.network.proto.thread.PbPageResponseLite
import app.tiebalite.core.network.proto.thread.ThreadAdParamLite
import app.tiebalite.core.network.proto.thread.ThreadAppPosInfoLite
import app.tiebalite.core.network.proto.thread.ThreadCommonReqLite
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class PbPageNetworkSource(
    private val api: PbPageApi,
) {
    private val identity = DeviceIdentity.create()

    suspend fun fetchPage(
        threadId: Long,
        page: Int = 1,
        postId: Long = 0L,
        seeLz: Boolean = false,
        sortType: Int = 0,
        forumId: Long? = null,
        back: Boolean = false,
        lastPostId: Long? = null,
        cmd: Int = NetworkDefaults.PB_PAGE_CMD,
        clientUserToken: String? = null,
        bduss: String? = null,
        stoken: String? = null,
        tbs: String? = null,
    ): Result<PbPageRaw> {
        return try {
            val requestBytes =
                buildRequestBody(
                    threadId = threadId,
                    page = page,
                    postId = postId,
                    seeLz = seeLz,
                    sortType = sortType,
                    forumId = forumId,
                    back = back,
                    lastPostId = lastPostId,
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
                api.getPbPage(
                    cmd = cmd,
                    clientUserToken = clientUserToken,
                    cookie = buildCookie(identity.cuid),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    formParts = formParts,
                    data = dataPart,
                ).bytes()
            val response = PbPageResponseLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("pb page api failed: $errorNo $errorMessage")
            }
            Result.success(
                PbPageRaw(
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
        page: Int,
        postId: Long,
        seeLz: Boolean,
        sortType: Int,
        forumId: Long?,
        back: Boolean,
        lastPostId: Long?,
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

        val appPos =
            ThreadAppPosInfoLite.newBuilder()
                .setApMac("02:00:00:00:00:00")
                .setApConnected(true)
                .setCoordinateType("BD09LL")
                .setAddrTimestamp(0L)
                .setAspShownInfo("")
                .build()

        val adParam =
            ThreadAdParamLite.newBuilder()
                .setLoadCount(0)
                .setRefreshCount(1)
                .setIsReqAd(1)
                .build()

        return PbPageRequestLite.newBuilder()
            .setData(
                PbPageRequestDataLite.newBuilder()
                    .setPbRn(0)
                    .setMark(0)
                    .setBack(if (back) 1 else 0)
                    .setKz(threadId)
                    .setLz(if (seeLz) 1 else 0)
                    .setR(sortType)
                    .setPid(postId)
                    .setWithFloor(1)
                    .setFloorRn(4)
                    .setRn(15)
                    .setScrW(scrW)
                    .setScrH(scrH)
                    .setScrDip(scrDip)
                    .setQType(2)
                    .setPn(page.coerceAtLeast(1))
                    .setCommon(common)
                    .setIsCommReverse(0)
                    .setObjSource("")
                    .setObjLocate("")
                    .setObjParam1("10")
                    .setAppPos(appPos)
                    .setForumId(forumId ?: 0L)
                    .setAdParam(adParam)
                    .setOriUgcType(0)
                    .setFromPush(0)
                    .setFloorSortType(1)
                    .setSourceType(2)
                    .setImmersionVideoCommentSource(0)
                    .setIsFoldCommentReq(0)
                    .setRequestTimes(0)
                    .setLastPid(lastPostId ?: 0L)
                    .build(),
            ).build()
            .toByteArray()
    }

    private fun buildCookie(cuid: String): String = "ka=open;CUID=$cuid;TBBRAND=${Build.MODEL};"
}

private val PlainTextMediaType = "text/plain".toMediaType()
private val BinaryMediaType = "application/octet-stream".toMediaType()

private const val From = "1020031h"
private const val DefaultScrW = 1080
private const val DefaultScrH = 2400
private const val DefaultScrDip = 3.0

private data class DeviceIdentity(
    val clientId: String,
    val cuid: String,
    val cuidGalaxy2: String,
    val c3Aid: String,
) {
    companion object {
        fun create(): DeviceIdentity {
            val initTime = System.currentTimeMillis()
            val clientId = "wappc_${initTime}_${(Math.random() * 1000).roundToInt()}"
            val cuid = java.util.UUID.randomUUID().toString().replace("-", "")
            val c3Aid = java.util.UUID.randomUUID().toString().replace("-", "")
            return DeviceIdentity(
                clientId = clientId,
                cuid = cuid,
                cuidGalaxy2 = cuid,
                c3Aid = c3Aid,
            )
        }
    }
}
