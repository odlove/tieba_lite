package app.tiebalite.feature.thread.subposts.state

sealed interface ThreadSubPostsUiEvent {
    data class ShowToast(
        val message: String,
    ) : ThreadSubPostsUiEvent
}
