package app.tiebalite.feature.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tiebalite.core.model.thread.ThreadPost
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    paddingValues: PaddingValues,
    state: ThreadUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding(),
        )

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        ThreadTopBar(
            state = state,
            onBack = onBack,
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = rememberPullToRefreshState(),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (state.isInitialLoading && state.posts.isEmpty()) {
                ThreadLoading()
            } else if (state.posts.isEmpty() && state.errorMessage != null) {
                ThreadError(
                    message = state.errorMessage,
                    onRetry = onRetry,
                )
            } else if (state.posts.isEmpty()) {
                ThreadEmpty(onRetry = onRetry)
            } else {
                ThreadPostList(
                    threadTitle = state.title,
                    posts = state.posts,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadTopBar(
    state: ThreadUiState,
    onBack: () -> Unit,
) {
    val forumName = state.forumName?.trim()?.takeIf { it.isNotEmpty() }
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            CenterAlignedTopAppBar(
                title = {
                    if (forumName != null) {
                        ThreadForumChip(
                            forumName = forumName,
                            avatarUrl = state.forumAvatarUrl,
                        )
                    } else {
                        Text(
                            text = state.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun ThreadForumChip(
    forumName: String,
    avatarUrl: String?,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(100),
        modifier = Modifier.padding(horizontal = 48.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .height(30.dp)
                    .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ForumChipAvatar(avatarUrl = avatarUrl, fallbackText = forumName)
            Text(
                text = "${forumName}吧",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

@Composable
private fun ForumChipAvatar(
    avatarUrl: String?,
    fallbackText: String,
) {
    val imageUrl = avatarUrl?.trim().orEmpty()
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            modifier =
                Modifier
                    .size(22.dp)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        return
    }
    Box(
        modifier =
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = fallbackText.firstOrNull()?.toString().orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
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

@Composable
private fun ThreadPostList(
    threadTitle: String,
    posts: List<ThreadPost>,
    contentPadding: PaddingValues,
) {
    val firstPost = posts.firstOrNull { it.floor <= 1 } ?: posts.firstOrNull()
    val replyPosts =
        if (firstPost == null) {
            posts
        } else {
            posts.filterNot { it.id == firstPost.id }
        }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        if (firstPost != null) {
            item(key = "first_post") {
                ThreadFirstFloorCard(
                    threadTitle = threadTitle,
                    item = firstPost,
                )
            }
            if (replyPosts.isNotEmpty()) {
                item(key = "first_post_divider") {
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
        }
        itemsIndexed(
            items = replyPosts,
            key = { _, item -> item.id },
        ) { index, item ->
            ThreadReplyItem(item = item)
            if (index < replyPosts.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun ThreadFirstFloorCard(
    threadTitle: String,
    item: ThreadPost,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AuthorAvatar(
                name = item.authorName,
                imageUrl = item.authorAvatarUrl,
                size = 38.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AuthorNameWithLevel(
                    name = item.authorName ?: "匿名用户",
                    level = item.authorLevel,
                    textStyle = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = formatPostMeta(item.timestampSeconds, item.ipLocation),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "${item.floor.coerceAtLeast(1)}楼",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (threadTitle.isNotBlank()) {
            Text(
                text = threadTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (item.contentText.isNotBlank()) {
            Text(
                text = item.contentText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        PostImageGrid(imageUrls = item.imageUrls)
    }
}

@Composable
private fun ThreadReplyItem(
    item: ThreadPost,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthorNameWithLevel(
                name = item.authorName ?: "匿名用户",
                level = item.authorLevel,
                textStyle = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${item.floor}楼",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (item.contentText.isNotBlank()) {
            Text(
                text = item.contentText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (item.imageUrls.isNotEmpty()) {
            Text(
                text = "图片 ${item.imageUrls.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AuthorNameWithLevel(
    name: String,
    level: Int,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (level > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            LevelChip(level = level)
        }
    }
}

@Composable
private fun LevelChip(level: Int) {
    val chipColor = levelColor(level)
    Text(
        text = level.toString(),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 10.sp),
        color = chipColor,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .width(24.dp)
                .clip(RoundedCornerShape(percent = 100))
                .background(chipColor.copy(alpha = 0.14f))
                .padding(vertical = 1.dp),
        maxLines = 1,
    )
}

private fun levelColor(level: Int): Color =
    when (level) {
        in 1..3 -> Color(0xFF2FBEAB)
        in 4..9 -> Color(0xFF3AA7E9)
        in 10..15 -> Color(0xFFFFA126)
        in 16..18 -> Color(0xFFFF9C19)
        else -> Color(0xFFB7BCB6)
    }

@Composable
private fun PostImageGrid(imageUrls: List<String>) {
    if (imageUrls.isEmpty()) {
        return
    }
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val containerWidthDp = with(LocalDensity.current) { containerWidthPx.toDp() }
    val widthFraction = if (containerWidthDp < 600.dp) 1f else 0.5f
    Column(
        modifier = Modifier.fillMaxWidth(widthFraction),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        imageUrls.forEach { imageUrl ->
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}

@Composable
private fun AuthorAvatar(
    name: String?,
    imageUrl: String?,
    size: androidx.compose.ui.unit.Dp,
) {
    val avatarUrl = imageUrl?.trim().orEmpty()
    if (avatarUrl.isNotBlank()) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            modifier =
                Modifier
                    .size(size)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        return
    }
    Box(
        modifier =
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name?.firstOrNull()?.toString().orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

private fun formatPostMeta(
    seconds: Long?,
    ipLocation: String?,
): String {
    val values =
        listOfNotNull(
            seconds
                ?.takeIf { it > 0L }
                ?.let { value ->
                    SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINA).format(Date(value * 1000))
                },
            ipLocation?.trim()?.takeIf { it.isNotEmpty() },
        )
    return values.joinToString(" · ").ifBlank { "未知时间" }
}
