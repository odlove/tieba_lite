package app.tiebalite.core.network.source.tbclient

import app.tiebalite.core.network.client.TbClientDevice
import app.tiebalite.core.network.client.TbClientIdentity

internal val TestTbClientIdentity =
    TbClientIdentity(
        clientId = "wappc_1_2",
        cuid = "cuid",
        cuidGalaxy2 = "cuid",
        c3Aid = "c3aid",
    )

internal val TestTbClientDevice =
    TbClientDevice(
        model = "Android",
        brand = "",
        osVersion = "15",
    )

internal val TestTbClientScreen =
    TbClientScreen(
        width = 1080,
        height = 2400,
        density = 3.0,
    )

internal const val TestTbClientTimestamp = 0L
