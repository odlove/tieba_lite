package app.tiebalite.feature.history

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.history.ThreadHistoryRecord
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.format.formatDateTime
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun HistoryScreen(
    paddingValues: PaddingValues,
    state: HistoryUiState,
    onOpenThread: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val bottomInset =
        WindowInsets.safeDrawing
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues()
            .calculateBottomPadding()
    val contentPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection) + HistoryListHorizontalPadding,
            end = paddingValues.calculateEndPadding(layoutDirection) + HistoryListHorizontalPadding,
            top = 0.dp,
            bottom = bottomInset,
        )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.history_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
        )

        if (state.items.isEmpty()) {
            HistoryEmpty(
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                itemsIndexed(
                    items = state.items,
                    key = { _, item -> item.threadId },
                ) { index, item ->
                    HistoryItem(
                        item = item,
                        onClick = { onOpenThread(item.threadId) },
                    )
                    if (index < state.items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = HistoryItemHorizontalPadding),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEmpty(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.history_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HistoryItem(
    item: ThreadHistoryRecord,
    onClick: () -> Unit,
) {
    val forumName = item.forumName?.takeIf { it.isNotBlank() }
    val forumAvatarUrl = item.forumAvatarUrl?.takeIf { it.isNotBlank() }
    val title =
        item.title.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.history_thread_fallback, item.threadId)
    val visitedAt = formatDateTime(item.lastEnteredAt / 1000).orEmpty()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = HistoryItemHorizontalPadding,
                    vertical = HistoryItemVerticalPadding,
                ),
        verticalArrangement = Arrangement.spacedBy(HistoryItemContentSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HistoryTopRowSpacing),
        ) {
            forumName?.let {
                HistoryForumChip(
                    name = it,
                    avatarUrl = forumAvatarUrl,
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = visitedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HistoryForumChip(
    name: String,
    avatarUrl: String?,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = HistoryForumChipHorizontalPadding,
                    vertical = HistoryForumChipVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HistoryForumChipContentSpacing),
        ) {
            avatarUrl?.let {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(it)
                            .crossfade(true)
                            .build(),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(HistoryForumChipAvatarSize)
                            .clip(MaterialTheme.shapes.extraSmall),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(
                text = "${name}吧",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// Page-level outer spacing.
private val HistoryListHorizontalPadding = 16.dp

// Item-level spacing.
private val HistoryItemHorizontalPadding = 12.dp
private val HistoryItemVerticalPadding = 10.dp
private val HistoryItemContentSpacing = 6.dp
private val HistoryTopRowSpacing = 12.dp

// Forum chip spacing and avatar size.
private val HistoryForumChipHorizontalPadding = 6.dp
private val HistoryForumChipVerticalPadding = 4.dp
private val HistoryForumChipContentSpacing = 6.dp
private val HistoryForumChipAvatarSize = 18.dp
