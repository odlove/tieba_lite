package app.tiebalite.core.data.recommend.mapper

import app.tiebalite.core.data.common.mapper.FeedItemMappingContext
import app.tiebalite.core.data.common.mapper.toRecommendItem
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw

class PersonalizedFeedMapper {
    fun map(raw: PersonalizedFeedRaw): List<RecommendItem> {
        val data = raw.response.data
        val context =
            FeedItemMappingContext(
                threadAuthorMap =
                    data.threadListList.mapNotNull { thread ->
                        val threadId = thread.tid.takeIf { it > 0L } ?: thread.id.takeIf { it > 0L }
                        threadId?.let { it to thread.author }
                    }.toMap(),
                includeForum = true,
            )
        return data.pageData.feedListList.mapNotNull { layout ->
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
