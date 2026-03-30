package app.tiebalite.feature.thread.main.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
internal fun ThreadLoadMoreEffect(
    listState: LazyListState,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    canLoadMoreBelow: Boolean,
    onLoadMore: () -> Unit,
) {
    val shouldLoadMore by remember(listState, canLoadMoreBelow) {
        derivedStateOf {
            if (!canLoadMoreBelow) {
                return@derivedStateOf false
            }
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - LoadMorePrefetchDistance
        }
    }

    LaunchedEffect(shouldLoadMore, isRefreshing, isLoadingMore, canLoadMoreBelow) {
        if (shouldLoadMore && !isRefreshing && !isLoadingMore) {
            onLoadMore()
        }
    }
}
private const val LoadMorePrefetchDistance = 3
