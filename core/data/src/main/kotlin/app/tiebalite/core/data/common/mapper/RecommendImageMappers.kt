package app.tiebalite.core.data.common.mapper

import app.tiebalite.core.model.recommend.RecommendImage
import app.tiebalite.core.network.proto.recommend.MediaLite

internal fun MediaLite.toRecommendImage(): RecommendImage? {
    if (type != 0 && type != RecommendMediaTypeImage) {
        return null
    }
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

private const val RecommendMediaTypeImage = 3
