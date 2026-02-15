package app.tiebalite.core.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.recommend.RecommendItem
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun FeedCardHeader(item: RecommendItem) {
    val subtitle = formatLastTime(item.lastTimeTimestampSeconds)
    val forumName = item.forumName?.trim()?.takeIf { it.isNotEmpty() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FeedAvatar(
            name = item.authorName,
            imageUrl = item.authorAvatarUrl,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.authorName ?: "匿名用户",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        forumName?.let {
            Text(
                text = "${it}吧",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatLastTime(lastTimeSeconds: Long?): String? {
    if (lastTimeSeconds != null && lastTimeSeconds > 0L) {
        return formatDate(Date(lastTimeSeconds * 1000))
    }
    return null
}

private fun formatDate(date: Date): String =
    SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINA).format(date)

@Composable
private fun FeedAvatar(
    name: String?,
    imageUrl: String?,
) {
    val text =
        name
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.first()
            ?.toString()
            ?: "?"
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
        )
        return
    }
    Box(
        modifier =
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
