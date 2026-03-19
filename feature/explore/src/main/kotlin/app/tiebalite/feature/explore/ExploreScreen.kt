package app.tiebalite.feature.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.core.model.imageviewer.ImageViewerItem
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.feed.FeedCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    paddingValues: PaddingValues,
    state: ExploreUiState,
    onOpenThread: (String) -> Unit,
    onOpenForum: (String) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding(),
        )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "推荐")

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = rememberPullToRefreshState(),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (state.isInitialLoading && state.items.isEmpty()) {
                ExploreLoading()
            } else if (state.items.isEmpty() && state.errorMessage != null) {
                ExploreError(
                    message = state.errorMessage,
                    onRetry = onRetry,
                )
            } else if (state.items.isEmpty()) {
                ExploreEmpty(onRetry = onRetry)
            } else {
                ExploreList(
                    items = state.items,
                    contentPadding = contentPadding,
                    isRefreshing = state.isRefreshing,
                    isLoadingMore = state.isLoadingMore,
                    onOpenThread = onOpenThread,
                    onOpenForum = onOpenForum,
                    onOpenImageViewer = onOpenImageViewer,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

@Composable
private fun ExploreLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ExploreEmpty(
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "暂无动态内容",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = "刷新")
            }
        }
    }
}

@Composable
private fun ExploreError(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = "重试")
            }
        }
    }
}

@Composable
private fun ExploreList(
    items: List<RecommendItem>,
    contentPadding: PaddingValues,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    onOpenThread: (String) -> Unit,
    onOpenForum: (String) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    LoadMoreEffect(
        listState = listState,
        isRefreshing = isRefreshing,
        isLoadingMore = isLoadingMore,
        onLoadMore = onLoadMore,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = contentPadding,
    ) {
        itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
            FeedCard(
                item = item,
                onClick = {
                    onOpenThread(item.id)
                },
                onOpenForum = onOpenForum,
                onOpenMedia = {
                    item.toImageViewerArgs()?.let(onOpenImageViewer)
                },
            )
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
        if (isLoadingMore) {
            item(key = "load_more_footer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun LoadMoreEffect(
    listState: LazyListState,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - LoadMorePrefetchDistance
        }
    }

    LaunchedEffect(shouldLoadMore, isRefreshing, isLoadingMore) {
        if (shouldLoadMore && !isRefreshing && !isLoadingMore) {
            onLoadMore()
        }
    }
}

private const val LoadMorePrefetchDistance = 3

private fun RecommendItem.toImageViewerArgs(): ImageViewerArgs? {
    if (images.isEmpty()) {
        return null
    }
    return ImageViewerArgs(
        items =
            images.mapIndexed { index, image ->
                ImageViewerItem(
                    id = "${id}_$index",
                    imageUrl = image.url,
                    width = image.width,
                    height = image.height,
                )
            },
        initialIndex = 0,
    )
}
