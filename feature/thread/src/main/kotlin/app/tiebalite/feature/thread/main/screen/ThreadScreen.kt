package app.tiebalite.feature.thread.main.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.feature.thread.main.state.ThreadUiState
import app.tiebalite.feature.thread.main.list.ThreadPostList
import app.tiebalite.feature.thread.main.screen.ThreadTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    paddingValues: PaddingValues,
    state: ThreadUiState,
    onBack: () -> Unit,
    onOpenForum: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpenSubPosts: (Long) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
) {
    val hasContent = state.firstFloorPost != null || state.posts.isNotEmpty()
    val layoutDirection = LocalLayoutDirection.current
    val bottomInset =
        WindowInsets.safeDrawing
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues()
            .calculateBottomPadding()
    val contentPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding() + bottomInset + 12.dp,
        )

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        ThreadTopBar(
            state = state,
            onBack = onBack,
            onOpenForum = onOpenForum,
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = rememberPullToRefreshState(),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (state.isInitialLoading && !hasContent) {
                ThreadLoading()
            } else if (!hasContent && state.errorMessage != null) {
                ThreadError(
                    message = state.errorMessage,
                    onRetry = onRetry,
                )
            } else if (!hasContent) {
                ThreadEmpty(onRetry = onRetry)
            } else {
                ThreadPostList(
                    firstFloorPost = state.firstFloorPost,
                    replyPosts = state.posts,
                    contentPadding = contentPadding,
                    isRefreshing = state.isRefreshing,
                    isLoadingMore = state.isLoadingMore,
                    hasMore = state.hasMore,
                    onLoadMore = onLoadMore,
                    onOpenSubPosts = onOpenSubPosts,
                    onOpenImageViewer = onOpenImageViewer,
                )
            }
        }
    }
}

@Composable
private fun ThreadLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ThreadError(
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
private fun ThreadEmpty(
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
                text = "暂无帖子内容",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = "刷新")
            }
        }
    }
}
