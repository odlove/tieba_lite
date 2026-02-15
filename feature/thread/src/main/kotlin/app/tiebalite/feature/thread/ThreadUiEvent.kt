package app.tiebalite.feature.thread

sealed interface ThreadUiEvent {
    data class ShowToast(
        val message: String,
    ) : ThreadUiEvent
}
