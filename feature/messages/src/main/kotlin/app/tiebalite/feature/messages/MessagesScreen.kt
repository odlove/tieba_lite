package app.tiebalite.feature.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tiebalite.core.ui.components.AppTopBar

@Composable
fun MessagesScreen(paddingValues: PaddingValues) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "消息")
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
        )
    }
}
