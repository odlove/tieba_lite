package app.tiebalite.feature.thread.subposts.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.feature.thread.main.list.ThreadLoadMoreEffect
import app.tiebalite.feature.thread.subposts.post.ThreadParentPostCard
import app.tiebalite.feature.thread.subposts.post.ThreadSubPostCard
import app.tiebalite.feature.thread.subposts.state.ThreadSubPostsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThreadSubPostsScreen(
    paddingValues: PaddingValues,
    state: ThreadSubPostsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
) {
    val postFloor = state.post?.floor
    val title = if (postFloor != null && postFloor > 0) "楼中楼 ${postFloor}楼" else "楼中楼"
    val layoutDirection = LocalLayoutDirection.current
    val listState = rememberLazyListState()
    val bottomInset =
        WindowInsets.safeDrawing
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues()
            .calculateBottomPadding()
    val listPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding() + bottomInset + 12.dp,
        )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isInitialLoading && state.post == null && state.subPosts.isEmpty()) {
            ThreadSubPostsLoading(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
            return@Scaffold
        }
        if (state.errorMessage != null && state.post == null && state.subPosts.isEmpty()) {
            ThreadSubPostsError(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                message = state.errorMessage,
                onRetry = onRetry,
            )
            return@Scaffold
        }

        ThreadLoadMoreEffect(
            listState = listState,
            isRefreshing = state.isRefreshing,
            isLoadingMore = state.isLoadingMore,
            canLoadMoreBelow = state.hasMore,
            onLoadMore = onLoadMore,
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = rememberPullToRefreshState(),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = listPadding,
            ) {
                state.post?.let { post ->
                    item(key = "subposts_parent_post") {
                        ThreadParentPostCard(
                            item = post.copy(subPostCount = 0),
                            threadAuthorId = state.threadAuthorId,
                            onOpenImageViewer = onOpenImageViewer,
                        )
                    }
                    item(key = "subposts_parent_divider") {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }

                item(key = "subposts_header") {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "全部楼中楼 ${state.totalCount.takeIf { it > 0 } ?: state.subPosts.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                itemsIndexed(
                    items = state.subPosts,
                    key = { _, item -> item.id },
                ) { index, item ->
                    ThreadSubPostCard(
                        item = item,
                        threadAuthorId = state.threadAuthorId,
                        onOpenImageViewer = onOpenImageViewer,
                    )
                    if (index < state.subPosts.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }

                if (state.hasMore && state.isLoadingMore) {
                    item(key = "subposts_load_more") {
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
        }
    }
}

@Composable
private fun ThreadSubPostsLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ThreadSubPostsError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
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
            TextButton(onClick = onRetry) {
                Text(text = "重试")
            }
        }
    }
}
