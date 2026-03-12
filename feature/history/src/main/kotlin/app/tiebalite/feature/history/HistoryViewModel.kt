package app.tiebalite.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.tiebalite.core.data.history.repository.ThreadHistoryRepository
import app.tiebalite.core.data.history.repository.ThreadHistoryRepositoryFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: ThreadHistoryRepository,
) : ViewModel() {
    private val mutableUiEvents = MutableSharedFlow<HistoryUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<HistoryUiEvent> = mutableUiEvents.asSharedFlow()

    val uiState =
        repository.observeThreadHistory()
            .map(::HistoryUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = HistoryUiState(),
            )

    fun deleteThreadHistory(threadId: Long) {
        viewModelScope.launch {
            runCatching {
                repository.deleteThreadHistory(threadId)
            }.onFailure {
                emitToast(OPERATION_FAILED_MESSAGE)
            }
        }
    }

    fun clearThreadHistory() {
        viewModelScope.launch {
            runCatching {
                repository.clearThreadHistory()
            }.onFailure {
                emitToast(OPERATION_FAILED_MESSAGE)
            }
        }
    }

    private fun emitToast(message: String) {
        mutableUiEvents.tryEmit(HistoryUiEvent.ShowToast(message))
    }

    companion object {
        private const val OPERATION_FAILED_MESSAGE = "操作失败"

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(this[APPLICATION_KEY])
                    HistoryViewModel(
                        repository = ThreadHistoryRepositoryFactory.create(application),
                    )
                }
            }
    }
}

sealed interface HistoryUiEvent {
    data class ShowToast(
        val message: String,
    ) : HistoryUiEvent
}
