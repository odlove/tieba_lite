package app.tiebalite.feature.history

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HistoryRoute(
    paddingValues: PaddingValues,
    onOpenThread: (Long) -> Unit,
    onOpenForum: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory),
) {
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is HistoryUiEvent.ShowToast -> {
                    Toast.makeText(currentContext, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HistoryScreen(
        paddingValues = paddingValues,
        state = uiState,
        onOpenThread = onOpenThread,
        onOpenForum = onOpenForum,
        onBack = onBack,
    )
}
