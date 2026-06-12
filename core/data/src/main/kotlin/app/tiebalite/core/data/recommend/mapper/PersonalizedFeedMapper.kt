package app.tiebalite.core.data.recommend.mapper

import app.tiebalite.core.data.common.mapper.FeedItemMappingContext
import app.tiebalite.core.data.common.mapper.toRecommendItem
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw

class PersonalizedFeedMapper {
    fun map(raw: PersonalizedFeedRaw): List<RecommendItem> {
        val context = FeedItemMappingContext(includeForum = true)
        return raw.response.data.pageData.feedListList.mapNotNull { layout ->
            if (layout.layout != FEED_LAYOUT) {
                return@mapNotNull null
            }
            layout.feed.toRecommendItem(context)
        }
    }

    private companion object {
        private const val FEED_LAYOUT = "feed"
    }
}
