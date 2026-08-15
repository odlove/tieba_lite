package app.tiebalite.feature.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.forum.ForumHeader
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.core.model.imageviewer.ImageViewerItem
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.feed.FeedCard
import app.tiebalite.core.ui.components.video.InlineVideoPlayer
import app.tiebalite.core.ui.components.video.rememberInlineVideoPlayback
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    paddingValues: PaddingValues,
    forumName: String,
    state: ForumUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onToggleForumLike: () -> Unit,
    onSignInForum: () -> Unit,
    onOpenThread: (String) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding() + 12.dp,
        )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = (state.header?.forumName ?: forumName) + "吧",
            navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
            onNavigationClick = onBack,
            actions = {
                if (state.header?.isLiked == true) {
                    ForumMoreMenu(
                        enabled = !state.isFollowUpdating && !state.isSignUpdating,
                        onUnfollow = onToggleForumLike,
                    )
                }
            },
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = rememberPullToRefreshState(),
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                state.isInitialLoading && state.header == null && state.items.isEmpty() -> ForumLoading()
                state.header == null && state.items.isEmpty() && state.errorMessage != null ->
                    ForumError(
                        message = state.errorMessage,
                        onRetry = onRetry,
                    )
                state.header == null && state.items.isEmpty() -> ForumEmpty()
                else -> ForumContent(
                    contentPadding = contentPadding,
                    state = state,
                    onToggleForumLike = onToggleForumLike,
                    onSignInForum = onSignInForum,
                    onOpenThread = onOpenThread,
                    onOpenImageViewer = onOpenImageViewer,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

@Composable
private fun ForumContent(
    contentPadding: PaddingValues,
    state: ForumUiState,
    onToggleForumLike: () -> Unit,
    onSignInForum: () -> Unit,
    onOpenThread: (String) -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    val videoPlayback = rememberInlineVideoPlayback()
    val (stickyItems, regularItems) =
        remember(state.items) {
            state.items.partition { item -> item.isTop }
        }

    LaunchedEffect(state.isRefreshing) {
        if (state.isRefreshing) {
            videoPlayback.stop()
        }
    }
    LaunchedEffect(regularItems, videoPlayback.playingItemId) {
        val playingId = videoPlayback.playingItemId
        if (playingId != null && regularItems.none { item -> item.id == playingId }) {
            videoPlayback.stop()
        }
    }
    ForumLoadMoreEffect(
        listState = listState,
        isRefreshing = state.isRefreshing,
        isLoadingMore = state.isLoadingMore,
        hasMore = state.hasMore,
        onLoadMore = onLoadMore,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = contentPadding,
    ) {
        state.header?.let { header ->
            item(key = "forum_header") {
                ForumHeaderCard(
                    header = header,
                    isFollowUpdating = state.isFollowUpdating,
                    isSignUpdating = state.isSignUpdating,
                    onToggleForumLike = onToggleForumLike,
                    onSignInForum = onSignInForum,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }
            item(key = "forum_header_divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }

        if (stickyItems.isNotEmpty()) {
            item(key = "forum_sticky_threads") {
                ForumStickyThreadsSection(
                    items = stickyItems,
                    onOpenThread = onOpenThread,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            if (regularItems.isNotEmpty()) {
                item(key = "forum_sticky_threads_divider") {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }

        if (state.items.isEmpty()) {
            item(key = "forum_empty_hint") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无帖子内容",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        itemsIndexed(
            items = regularItems,
            key = { _, item -> item.id },
        ) { index, item ->
            val isVideoPlaying = videoPlayback.playingItemId == item.id
            FeedCard(
                item = item,
                isVideoPlaying = isVideoPlaying,
                hasRenderedFirstFrame = videoPlayback.hasRenderedFirstFrame(item.id),
                videoPlayer = if (isVideoPlaying) videoPlayback.player else null,
                videoPlayerContent =
                    if (isVideoPlaying) {
                        {
                            InlineVideoPlayer(
                                player = videoPlayback.player,
                                modifier = Modifier.fillMaxSize(),
                                onVisibilityChanged = { isVisible ->
                                    if (!isVisible) {
                                        videoPlayback.pauseIfPlaying(item.id)
                                    }
                                },
                                onViewReleased = {
                                    videoPlayback.pauseIfPlaying(item.id)
                                },
                            )
                        }
                    } else {
                        null
                    },
                onClick = { onOpenThread(item.id) },
                onOpenMedia = {
                    item.toImageViewerArgs()?.let(onOpenImageViewer)
                },
                onPlayVideo = {
                    val videoUrl = item.video?.url ?: return@FeedCard
                    videoPlayback.play(
                        itemId = item.id,
                        videoUrl = videoUrl,
                    )
                },
            )
            if (index < regularItems.lastIndex || state.isLoadingMore) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }

        if (state.isLoadingMore) {
            item(key = "forum_load_more") {
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
private fun ForumStickyThreadsSection(
    items: List<RecommendItem>,
    onOpenThread: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            ForumStickyThreadItem(
                item = item,
                onClick = { onOpenThread(item.id) },
            )
            if (index < items.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun ForumStickyThreadItem(
    item: RecommendItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "置顶",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = item.titleText,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ForumHeaderCard(
    header: ForumHeader,
    isFollowUpdating: Boolean,
    isSignUpdating: Boolean,
    onToggleForumLike: () -> Unit,
    onSignInForum: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ForumAvatar(
                avatarUrl = header.avatarUrl,
                forumName = header.forumName,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "${header.forumName}吧",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                header.slogan?.takeIf { it.isNotBlank() }?.let { slogan ->
                    Text(
                        text = slogan,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(
                modifier = Modifier.align(Alignment.CenterVertically),
                contentAlignment = Alignment.CenterEnd,
            ) {
                ForumHeaderAction(
                    header = header,
                    isFollowUpdating = isFollowUpdating,
                    isSignUpdating = isSignUpdating,
                    onFollow = onToggleForumLike,
                    onSignIn = onSignInForum,
                )
            }
        }

        if (header.isLiked && (header.userLevel > 0 || header.nextLevelScore > 0 || !header.levelName.isNullOrBlank())) {
            ForumLevelProgress(header = header)
        }

        ForumStatsPanel(header = header)
    }
}

@Composable
private fun ForumHeaderAction(
    header: ForumHeader,
    isFollowUpdating: Boolean,
    isSignUpdating: Boolean,
    onFollow: () -> Unit,
    onSignIn: () -> Unit,
) {
    when {
        !header.isLiked ->
            ForumActionButton(
                text = "关注",
                isLoading = isFollowUpdating,
                enabled = !isSignUpdating,
                onClick = onFollow,
            )
        !header.isSigned ->
            ForumActionButton(
                text = "签到",
                isLoading = isSignUpdating,
                enabled = !isFollowUpdating,
                onClick = onSignIn,
            )
        else -> ForumSignedStatusAction(header = header)
    }
}

@Composable
private fun ForumSignedStatusAction(
    header: ForumHeader,
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(
                minWidth = ButtonDefaults.MinWidth,
                minHeight = ButtonDefaults.MinHeight,
            )
            .padding(ButtonDefaults.ContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        ForumStatusBadge(header = header)
    }
}

@Composable
private fun ForumActionButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

@Composable
private fun ForumMoreMenu(
    enabled: Boolean,
    onUnfollow: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "更多选项",
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "取消关注",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                onClick = {
                    expanded = false
                    onUnfollow()
                },
                enabled = enabled,
                colors =
                    MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                    ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun ForumAvatar(
    avatarUrl: String?,
    forumName: String,
) {
    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        return
    }
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = forumName.firstOrNull()?.toString().orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ForumStatusBadge(
    header: ForumHeader,
) {
    val icon = when {
        header.isSigned -> Icons.Rounded.Check
        header.isLiked -> Icons.Rounded.Favorite
        else -> null
    }
    val text = when {
        header.isSigned -> header.continuousSignDays.takeIf { it > 0 }?.let { "已签到 ${it}天" } ?: "已签到"
        header.isLiked -> "待签到"
        else -> "未关注"
    }
    val contentColor = when {
        header.isSigned -> MaterialTheme.colorScheme.onPrimaryContainer
        header.isLiked -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Composable
private fun ForumLevelProgress(
    header: ForumHeader,
) {
    val progress =
        header.nextLevelScore
            .takeIf { it > 0 }
            ?.let { next ->
                (header.currentScore.toFloat() / next.toFloat()).coerceIn(0f, 1f)
            }
            ?: 0f

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Lv.${header.userLevel}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            header.levelName?.takeIf { it.isNotBlank() }?.let { levelName ->
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } ?: Spacer(modifier = Modifier.weight(1f))
            if (header.nextLevelScore > 0) {
                Text(
                    text = "${formatCount(header.currentScore)} / ${formatCount(header.nextLevelScore)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(100)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        )
    }
}

@Composable
private fun ForumStatsPanel(
    header: ForumHeader,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ForumStatItem(
            label = "成员",
            value = formatCount(header.memberCount),
            modifier = Modifier.weight(1f),
        )
        ForumStatDivider()
        ForumStatItem(
            label = "主题",
            value = formatCount(header.threadCount),
            modifier = Modifier.weight(1f),
        )
        ForumStatDivider()
        ForumStatItem(
            label = "帖子",
            value = formatCount(header.postCount),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ForumStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ForumStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    )
}

@Composable
private fun ForumLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ForumEmpty() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无帖子内容",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ForumError(
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = "重试")
            }
        }
    }
}

@Composable
private fun ForumLoadMoreEffect(
    listState: LazyListState,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
) {
    val shouldLoadMore by remember(listState, hasMore) {
        derivedStateOf {
            if (!hasMore) {
                return@derivedStateOf false
            }
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - LoadMorePrefetchDistance
        }
    }

    LaunchedEffect(shouldLoadMore, isRefreshing, isLoadingMore, hasMore) {
        if (shouldLoadMore && !isRefreshing && !isLoadingMore && hasMore) {
            onLoadMore()
        }
    }
}

private const val LoadMorePrefetchDistance = 3

private fun formatCount(value: Int): String {
    if (value <= 0) {
        return "0"
    }
    return when {
        value >= 10_000 -> String.format(java.util.Locale.US, "%.1f万", value / 10_000f)
        else -> value.toString()
    }
}

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
