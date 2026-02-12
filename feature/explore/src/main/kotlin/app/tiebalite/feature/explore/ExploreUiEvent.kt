package app.tiebalite.feature.explore

sealed interface ExploreUiEvent {
    data class ShowToast(
        val message: String,
    ) : ExploreUiEvent
}
