package app.tiebalite.feature.myforums

import app.tiebalite.core.model.myforums.MyForumItem

data class MyForumsUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val items: List<MyForumItem> = emptyList(),
    val errorMessage: String? = null,
)
