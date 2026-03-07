package app.tiebalite.feature.thread.main.state

sealed interface ThreadUiEvent {
    data class ShowToast(
        val message: String,
    ) : ThreadUiEvent
}
