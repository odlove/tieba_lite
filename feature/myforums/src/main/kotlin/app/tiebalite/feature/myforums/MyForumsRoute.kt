package app.tiebalite.feature.myforums

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
fun MyForumsRoute(
    paddingValues: PaddingValues,
    onOpenForum: (String) -> Unit,
    viewModel: MyForumsViewModel = viewModel(factory = MyForumsViewModel.Factory),
) {
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is MyForumsUiEvent.ShowToast -> {
                    Toast.makeText(currentContext, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    MyForumsScreen(
        paddingValues = paddingValues,
        state = uiState,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::refresh,
        onOpenForum = onOpenForum,
    )
}
