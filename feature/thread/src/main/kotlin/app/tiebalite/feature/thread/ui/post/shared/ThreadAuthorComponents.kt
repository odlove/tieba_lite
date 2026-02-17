package app.tiebalite.feature.thread.ui.post.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
internal fun AuthorNameWithLevel(
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
internal fun AuthorAvatar(
    name: String?,
    imageUrl: String?,
    size: Dp,
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
