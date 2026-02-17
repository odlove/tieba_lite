package app.tiebalite.feature.thread.state

sealed interface ThreadUiEvent {
    data class ShowToast(
        val message: String,
    ) : ThreadUiEvent
}
