package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.network.proto.frs.FrsPageResponseLite

data class FrsPageRaw(
    val body: ByteArray,
    val response: FrsPageResponseLite,
)
