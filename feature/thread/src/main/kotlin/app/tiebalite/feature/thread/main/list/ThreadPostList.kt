package app.tiebalite.feature.thread.main.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.core.model.thread.ThreadFirstFloorPost
import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.feature.thread.R
import app.tiebalite.feature.thread.main.post.ThreadFirstFloorCard
import app.tiebalite.feature.thread.main.post.ThreadReplyListItem
import app.tiebalite.feature.thread.main.state.ThreadReplySortType

@Composable
internal fun ThreadPostList(
    firstFloorPost: ThreadFirstFloorPost?,
    replyPosts: List<ThreadPost>,
    contentPadding: PaddingValues,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    canLoadMoreBelow: Boolean,
    seeLz: Boolean,
    sortType: Int,
    allowLoadLatestPosts: Boolean,
    onSetSeeLz: (Boolean) -> Unit,
    onSetSortType: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onOpenSubPosts: (Long) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
) {
    val listState = rememberLazyListState()
    val bottomPullState =
        rememberBottomPullToLoadLatestState(
            listState = listState,
            enabled = allowLoadLatestPosts,
            canLoadMoreBelow = canLoadMoreBelow,
            isRefreshing = isRefreshing,
            isLoadingMore = isLoadingMore,
            onTriggered = onLoadMore,
        )

    ThreadLoadMoreEffect(
        listState = listState,
        isRefreshing = isRefreshing,
        isLoadingMore = isLoadingMore,
        canLoadMoreBelow = canLoadMoreBelow,
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
                        onOpenImageViewer = onOpenImageViewer,
                    )
                }
                item(key = "first_floor_post_divider") {
                    androidx.compose.material3.HorizontalDivider(
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
                        ReplySeeLzToggle(
                            seeLz = seeLz,
                            onSetSeeLz = onSetSeeLz,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        ReplySortToggle(
                            sortType = sortType,
                            onSetSortType = onSetSortType,
                        )
                    }
                }
            }

            itemsIndexed(
                items = replyPosts,
                key = { _, item -> item.id },
            ) { index, item ->
                ThreadReplyListItem(
                    item = item,
                    threadAuthorId = firstFloorPost?.authorId,
                    onOpenSubPosts = onOpenSubPosts,
                    onOpenImageViewer = onOpenImageViewer,
                )
                if (index < replyPosts.lastIndex) {
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }

            if (canLoadMoreBelow && isLoadingMore) {
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

        if (allowLoadLatestPosts && !canLoadMoreBelow && (bottomPullState.pullDistancePx > 0f || isLoadingMore)) {
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

@Composable
private fun ReplySeeLzToggle(
    seeLz: Boolean,
    onSetSeeLz: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReplyHeaderToggleText(
            text = stringResource(R.string.thread_reply_header),
            selected = !seeLz,
            onClick = { onSetSeeLz(false) },
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ReplyHeaderToggleText(
            text = stringResource(R.string.thread_show_only_author),
            selected = seeLz,
            onClick = { onSetSeeLz(true) },
        )
    }
}

@Composable
private fun ReplySortToggle(
    sortType: Int,
    onSetSortType: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReplyHeaderToggleText(
            text = stringResource(R.string.thread_sort_ascending),
            selected = sortType == ThreadReplySortType.Ascending,
            onClick = { onSetSortType(ThreadReplySortType.Ascending) },
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ReplyHeaderToggleText(
            text = stringResource(R.string.thread_sort_descending),
            selected = sortType == ThreadReplySortType.Descending,
            onClick = { onSetSortType(ThreadReplySortType.Descending) },
        )
    }
}

@Composable
private fun ReplyHeaderToggleText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                enabled = !selected,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color =
            if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
    )
}
