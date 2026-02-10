package app.tiebalite.core.network.source.tbclient.recommend

import app.tiebalite.core.network.proto.recommend.PersonalizedResponseLite

data class PersonalizedFeedRaw(
    val body: ByteArray,
    val response: PersonalizedResponseLite,
)
