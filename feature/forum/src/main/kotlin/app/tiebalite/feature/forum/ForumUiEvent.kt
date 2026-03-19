package app.tiebalite.feature.forum

sealed interface ForumUiEvent {
    data class ShowToast(
        val message: String,
    ) : ForumUiEvent
}
