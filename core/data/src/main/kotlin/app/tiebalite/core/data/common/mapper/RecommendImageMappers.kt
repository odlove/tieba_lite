package app.tiebalite.core.data.common.mapper

import app.tiebalite.core.model.recommend.RecommendImage
import app.tiebalite.core.network.proto.recommend.MediaLite

internal fun MediaLite.toRecommendImage(): RecommendImage? {
    val url =
        normalizeUrl(originPic)
            ?: normalizeUrl(bigPic)
            ?: normalizeUrl(srcPic)
            ?: return null
    return RecommendImage(
        url = url,
        width = width.takeIf { it > 0 },
        height = height.takeIf { it > 0 },
    )
}
