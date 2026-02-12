package app.tiebalite.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.tiebalite.core.data.recommend.model.RecommendItem
import app.tiebalite.core.data.recommend.repository.RecommendRepository
import app.tiebalite.core.data.recommend.repository.RecommendRepositoryFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val repository: RecommendRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                _uiState.value = ExploreUiState.Loading
                _uiState.value =
                    repository.loadFeed().fold(
                        onSuccess = ::toSuccessState,
                        onFailure = { throwable ->
                            ExploreUiState.Error(
                                throwable.message ?: "请求失败，请稍后重试",
                            )
                        },
                    )
            }
    }

    private fun toSuccessState(items: List<RecommendItem>): ExploreUiState =
        if (items.isEmpty()) {
            ExploreUiState.Empty
        } else {
            ExploreUiState.Success(items)
        }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ExploreViewModel(
                        repository = RecommendRepositoryFactory.create(),
                    )
                }
            }
    }
}
