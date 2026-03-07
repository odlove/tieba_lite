package app.tiebalite.feature.thread.main

sealed interface ThreadUiEvent {
    data class ShowToast(
        val message: String,
    ) : ThreadUiEvent
}
