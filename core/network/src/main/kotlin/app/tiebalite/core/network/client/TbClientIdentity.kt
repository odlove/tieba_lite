package app.tiebalite.core.network.client

import java.util.UUID
import kotlin.math.roundToInt

internal data class TbClientIdentity(
    val clientId: String,
    val cuid: String,
    val cuidGalaxy2: String,
    val c3Aid: String,
) {
    companion object {
        val default: TbClientIdentity by lazy { create() }

        fun create(): TbClientIdentity {
            val initTime = System.currentTimeMillis()
            val cuid = UUID.randomUUID().toString().replace("-", "")
            val c3Aid = UUID.randomUUID().toString().replace("-", "")
            return TbClientIdentity(
                clientId = "wappc_${initTime}_${(Math.random() * 1000).roundToInt()}",
                cuid = cuid,
                cuidGalaxy2 = cuid,
                c3Aid = c3Aid,
            )
        }
    }
}
