package org.cryptimeleon.incentive.app.ui.setup

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme

@Composable
fun SetupUi(onFinished: () -> Unit) {
    val setupViewModel = hiltViewModel<SetupViewModel>()
    val setupFinished by setupViewModel.navigateToInfo.observeAsState(false)
    val feedback by setupViewModel.feedbackText.observeAsState("")

    LaunchedEffect(false) {
        setupViewModel.startSetup()
    }

    if (setupFinished) {
        LaunchedEffect(false) {
            onFinished()
        }
    }

    SetupUi(feedback = feedback)
}

@Composable
private fun SetupUi(feedback: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.size(8.dp))
        Text(feedback, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SetupUiPreview() {
    CryptimeleonTheme {
        SetupUi(feedback = "Loading Public Parameters")
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Scan Screen in Dark Mode"
)
fun SetupUiPreviewDark() {
    SetupUiPreview()
}

@Composable
@Preview(
    showBackground = true,
    name = "Scan Screen in Light Mode"
)
fun SetupUiPreviewLight() {
    SetupUiPreview()
}
