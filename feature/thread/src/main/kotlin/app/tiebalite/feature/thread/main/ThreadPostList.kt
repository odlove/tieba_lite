package app.tiebalite.feature.thread.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.thread.ThreadFirstFloorPost
import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.feature.thread.main.ThreadFirstFloorCard
import app.tiebalite.feature.thread.shared.ThreadPostCard

@Composable
internal fun ThreadPostList(
    firstFloorPost: ThreadFirstFloorPost?,
    replyPosts: List<ThreadPost>,
    contentPadding: PaddingValues,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onOpenSubPosts: (Long) -> Unit,
) {
    val listState = rememberLazyListState()
    val bottomPullState =
        rememberBottomPullToLoadLatestState(
            listState = listState,
            hasMore = hasMore,
            isRefreshing = isRefreshing,
            isLoadingMore = isLoadingMore,
            onTriggered = onLoadMore,
        )

    ThreadLoadMoreEffect(
        listState = listState,
        isRefreshing = isRefreshing,
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        onLoadMore = onLoadMore,
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .nestedScroll(bottomPullState.nestedScrollConnection),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
        ) {
            if (firstFloorPost != null) {
                item(key = "first_floor_post") {
                    ThreadFirstFloorCard(
                        item = firstFloorPost,
                    )
                }
                item(key = "first_floor_post_divider") {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                item(key = "reply_header") {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "全部回复 ${replyPosts.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            itemsIndexed(
                items = replyPosts,
                key = { _, item -> item.id },
            ) { index, item ->
                ThreadPostCard(
                    item = item,
                    threadAuthorId = firstFloorPost?.authorId,
                    onOpenSubPosts = onOpenSubPosts,
                )
                if (index < replyPosts.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }

            if (hasMore && isLoadingMore) {
                item(key = "thread_load_more_footer") {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        if (!hasMore && (bottomPullState.pullDistancePx > 0f || isLoadingMore)) {
            val indicatorLift =
                with(LocalDensity.current) {
                    (bottomPullState.pullDistancePx.coerceAtMost(bottomPullState.triggerDistancePx) / 4f)
                        .toDp()
                }
            ThreadLatestPostsPullIndicator(
                isLoading = isLoadingMore,
                isReady = bottomPullState.isReady,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp + indicatorLift),
            )
        }
    }
}
