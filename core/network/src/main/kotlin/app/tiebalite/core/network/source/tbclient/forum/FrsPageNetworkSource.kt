package app.tiebalite.core.network.source.tbclient.forum

import android.content.res.Resources
import android.net.Uri
import android.os.Build
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.proto.frs.FrsPageAdParamLite
import app.tiebalite.core.network.proto.frs.FrsPageRequestDataLite
import app.tiebalite.core.network.proto.frs.FrsPageRequestLite
import app.tiebalite.core.network.proto.frs.FrsPageResponseLite
import app.tiebalite.core.network.proto.recommend.AppPosInfoLite
import app.tiebalite.core.network.proto.recommend.CommonReqLite
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class FrsPageNetworkSource internal constructor(
    private val api: FrsPageApi,
) {
    private val identity = FrsDeviceIdentity.create()

    suspend fun fetchPage(
        forumName: String,
        page: Int = 1,
        loadType: Int = 1,
        sortType: Int = 0,
        goodClassifyId: Int? = null,
        clientUserToken: String? = null,
        bduss: String? = null,
        stoken: String? = null,
        tbs: String? = null,
    ): Result<FrsPageRaw> {
        return try {
            val requestBytes =
                buildRequestBody(
                    forumName = forumName,
                    page = page,
                    loadType = loadType,
                    sortType = sortType,
                    goodClassifyId = goodClassifyId,
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

            val encodedForumName = Uri.encode(forumName)
            val responseBytes =
                api.getFrsPage(
                    clientUserToken = clientUserToken,
                    cookie = buildCookie(identity.cuid),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    forumName = encodedForumName,
                    formParts = formParts,
                    data = dataPart,
                ).bytes()
            val response = FrsPageResponseLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("frs page api failed: $errorNo $errorMessage")
            }
            Result.success(
                FrsPageRaw(
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
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        goodClassifyId: Int?,
        bduss: String?,
        stoken: String?,
        tbs: String?,
    ): ByteArray {
        val metrics = Resources.getSystem().displayMetrics
        val scrW = metrics.widthPixels.takeIf { it > 0 } ?: DefaultScrW
        val scrH = metrics.heightPixels.takeIf { it > 0 } ?: DefaultScrH
        val scrDip = metrics.density.takeIf { it > 0f }?.toDouble() ?: DefaultScrDip

        val common =
            CommonReqLite.newBuilder()
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
            AppPosInfoLite.newBuilder()
                .setApMac("02:00:00:00:00:00")
                .setApConnected(true)
                .setCoordinateType("BD09LL")
                .setAddrTimestamp(0L)
                .setAspShownInfo("")
                .build()

        val adParam =
            FrsPageAdParamLite.newBuilder()
                .setLoadCount(0)
                .setRefreshCount(1)
                .setYogaLibVersion("")
                .build()

        return FrsPageRequestLite.newBuilder()
            .setData(
                FrsPageRequestDataLite.newBuilder()
                    .setKw(Uri.encode(forumName))
                    .setRn(90)
                    .setRnNeed(30)
                    .setIsGood(if (goodClassifyId != null) 1 else 0)
                    .setCid(goodClassifyId ?: 0)
                    .setWithGroup(1)
                    .setScrW(scrW)
                    .setScrH(scrH)
                    .setScrDip(scrDip)
                    .setQType(2)
                    .setPn(page.coerceAtLeast(1))
                    .setStType("recom_flist")
                    .setNetError(0)
                    .setCommon(common)
                    .setCategoryId(0)
                    .setYuelaouLocate("")
                    .setYuelaouParams("")
                    .setSortType(sortType)
                    .setLastClickTid(0L)
                    .setLoadType(loadType.coerceAtLeast(1))
                    .setAppPos(appPos)
                    .setAdParam(adParam)
                    .setObjLocate("")
                    .setObjSource("")
                    .setCallFrom(0)
                    .setUpSchema("")
                    .setRequestTimes(0)
                    .setIsNewfeed(0)
                    .setIsNewfrs(0)
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

private data class FrsDeviceIdentity(
    val clientId: String,
    val cuid: String,
    val cuidGalaxy2: String,
    val c3Aid: String,
) {
    companion object {
        fun create(): FrsDeviceIdentity {
            val initTime = System.currentTimeMillis()
            val cuid = UUID.randomUUID().toString().replace("-", "")
            val c3Aid = UUID.randomUUID().toString().replace("-", "")
            return FrsDeviceIdentity(
                clientId = "wappc_${initTime}_${(Math.random() * 1000).roundToInt()}",
                cuid = cuid,
                cuidGalaxy2 = cuid,
                c3Aid = c3Aid,
            )
        }
    }
}
