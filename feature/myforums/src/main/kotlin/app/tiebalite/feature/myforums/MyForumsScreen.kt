package app.tiebalite.feature.myforums

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.tiebalite.core.ui.components.AppTopBar
import coil3.compose.AsyncImage

@Composable
fun MyForumsScreen(
    paddingValues: PaddingValues,
    state: MyForumsUiState,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "我的吧")
        when {
            state.isLoading -> LoadingState(paddingValues = paddingValues)

            !state.isLoggedIn -> LoggedOutState(paddingValues = paddingValues)

            state.errorMessage != null ->
                ErrorState(
                    paddingValues = paddingValues,
                    message = state.errorMessage,
                    onRetry = onRetry,
                )

            state.items.isEmpty() -> EmptyState(paddingValues = paddingValues)

            else -> MyForumsList(
                paddingValues = paddingValues,
                state = state,
            )
        }
    }
}

@Composable
private fun MyForumsList(
    paddingValues: PaddingValues,
    state: MyForumsUiState,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 12.dp),
    ) {
        items(
            items = state.items,
            key = { item -> item.forumId },
        ) { item ->
            MyForumListItem(item = item)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun MyForumListItem(
    item: app.tiebalite.core.model.myforums.MyForumItem,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (item.avatarUrl != null) {
            AsyncImage(
                model = item.avatarUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.forumName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "热度 ${formatHotNum(item.hotNum)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Lv.${item.levelId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                if (item.isSigned) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(paddingValues: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoggedOutState(paddingValues: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = paddingValues.calculateBottomPadding()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "登录后查看你关注的吧",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(paddingValues: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = paddingValues.calculateBottomPadding()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "还没有关注任何吧",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(
    paddingValues: PaddingValues,
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = paddingValues.calculateBottomPadding()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text(text = "重试")
            }
        }
    }
}

private fun formatHotNum(value: Int): String {
    if (value < 10_000) {
        return value.toString()
    }
    return String.format(java.util.Locale.US, "%.1f万", value / 10_000f)
}
