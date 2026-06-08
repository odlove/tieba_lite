package app.tiebalite.feature.myforums

sealed interface MyForumsUiEvent {
    data class ShowToast(
        val message: String,
    ) : MyForumsUiEvent
}
