package app.tiebalite.feature.imageviewer.gesture

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.compose.zoom.Transform
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
internal fun rememberImageViewerDragDismissState(): ImageViewerDragDismissState {
    val scope = rememberCoroutineScope()
    val dismissThresholdPx = with(LocalDensity.current) { DismissThreshold.toPx() }
    val windowHeightPx = LocalWindowInfo.current.containerSize.height.toFloat().coerceAtLeast(1f)

    return remember(scope, dismissThresholdPx, windowHeightPx) {
        ImageViewerDragDismissState(
            scope = scope,
            dismissThresholdPx = dismissThresholdPx,
            windowHeightPx = windowHeightPx,
        )
    }
}

@Stable
internal class ImageViewerDragDismissState(
    private val scope: CoroutineScope,
    internal val dismissThresholdPx: Float,
    internal val windowHeightPx: Float,
) {
    var offsetY by mutableFloatStateOf(0f)
        private set

    var isDragging by mutableStateOf(false)
        private set

    private var animationJob: Job? = null

    private val dismissProgress: Float
        get() = (offsetY / (windowHeightPx * 0.5f)).coerceIn(0f, 1f)

    val viewerScale: Float
        get() = lerp(1f, DragDismissMinScale, dismissProgress)

    val viewerAlpha: Float
        get() = lerp(1f, DragDismissMinBackgroundAlpha, dismissProgress)

    val contentAlpha: Float
        get() = if (offsetY > 0f) viewerAlpha else 1f

    fun startDragging(initialOffsetY: Float) {
        animationJob?.cancel()
        isDragging = true
        offsetY = initialOffsetY.coerceAtLeast(0f)
    }

    fun dragBy(deltaY: Float) {
        offsetY = (offsetY + deltaY).coerceAtLeast(0f)
    }

    fun finishDragging(onDismissed: () -> Unit) {
        isDragging = false
        val currentOffsetY = offsetY
        val targetOffsetY =
            if (currentOffsetY >= dismissThresholdPx) {
                windowHeightPx
            } else {
                0f
            }
        animationJob?.cancel()
        animationJob =
            scope.launch {
                animate(
                    initialValue = currentOffsetY,
                    targetValue = targetOffsetY,
                    animationSpec =
                        if (targetOffsetY == windowHeightPx) {
                            tween(durationMillis = 180)
                        } else {
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            )
                        },
                ) { value, _ ->
                    offsetY = value
                }
                if (targetOffsetY == windowHeightPx) {
                    onDismissed()
                }
            }
    }
}

internal fun Modifier.imageViewerDragDismiss(
    enabled: Boolean,
    state: ImageViewerDragDismissState,
    viewConfiguration: ViewConfiguration,
    onDismissed: () -> Unit,
): Modifier =
    if (!enabled) {
        this
    } else {
        pointerInput(enabled, state.dismissThresholdPx, state.windowHeightPx) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var dragging = false
                var accumulatedX = 0f
                var accumulatedY = 0f

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        break
                    }

                    val delta = change.position - change.previousPosition
                    accumulatedX += delta.x
                    accumulatedY += delta.y

                    if (!dragging) {
                        val verticalDragDetected =
                            accumulatedY > viewConfiguration.touchSlop &&
                                accumulatedY > abs(accumulatedX)
                        val horizontalDragDetected =
                            abs(accumulatedX) > viewConfiguration.touchSlop &&
                                abs(accumulatedX) > accumulatedY
                        val upwardDragDetected = accumulatedY < -viewConfiguration.touchSlop

                        if (horizontalDragDetected || upwardDragDetected) {
                            break
                        }
                        if (verticalDragDetected) {
                            dragging = true
                            state.startDragging(
                                initialOffsetY =
                                    (accumulatedY - viewConfiguration.touchSlop).coerceAtLeast(0f),
                            )
                            change.consume()
                        }
                    } else {
                        state.dragBy(delta.y)
                        change.consume()
                    }
                }
                if (dragging) {
                    state.finishDragging(onDismissed)
                }
            }
        }
    }

internal fun Transform.isAtRestTransform(): Boolean =
    abs(scaleX - 1f) <= TransformEpsilon &&
        abs(scaleY - 1f) <= TransformEpsilon &&
        abs(offsetX) <= TransformOffsetEpsilon &&
        abs(offsetY) <= TransformOffsetEpsilon &&
        abs(rotation) <= TransformRotationEpsilon

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

private val DismissThreshold = 120.dp
private const val DragDismissMinScale = 0.82f
private const val DragDismissMinBackgroundAlpha = 0f
private const val TransformEpsilon = 0.01f
private const val TransformOffsetEpsilon = 2f
private const val TransformRotationEpsilon = 0.5f
