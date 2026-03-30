package app.tiebalite.feature.thread.main.state

sealed interface ThreadUiEvent {
    data class CopyThreadLink(
        val threadId: Long,
    ) : ThreadUiEvent

    data class ShowToast(
        val message: String,
    ) : ThreadUiEvent
}
