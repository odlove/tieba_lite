package app.tiebalite.core.network.client

import android.os.Build

internal data class TbClientDevice(
    val model: String,
    val brand: String,
    val osVersion: String,
) {
    companion object {
        val current: TbClientDevice
            get() =
                TbClientDevice(
                    model = Build.MODEL.orEmpty(),
                    brand = Build.BRAND.orEmpty(),
                    osVersion = Build.VERSION.RELEASE.orEmpty(),
                )
    }
}
