package app.tiebalite.feature.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.tiebalite.core.data.recommend.model.RecommendItem
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.components.feed.FeedCard

@Composable
fun ExploreScreen(
    paddingValues: PaddingValues,
    state: ExploreUiState,
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
        AppTopBar(title = "探索")

        when (state) {
            ExploreUiState.Loading -> ExploreLoading()
            ExploreUiState.Empty -> ExploreEmpty(onRetry = onRetry)
            is ExploreUiState.Error ->
                ExploreError(
                    message = state.message,
                    onRetry = onRetry,
                )
            is ExploreUiState.Success ->
                ExploreList(
                    items = state.items,
                    contentPadding = contentPadding,
                )
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
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
            FeedCard(item = item)
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}
