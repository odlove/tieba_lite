package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.proto.thread.PbPageResponseLite

data class PbPageRaw(
    val body: ByteArray,
    val response: PbPageResponseLite,
)
