package app.tiebalite.feature.history

import app.tiebalite.core.model.history.ThreadHistoryRecord

data class HistoryUiState(
    val items: List<ThreadHistoryRecord> = emptyList(),
)
