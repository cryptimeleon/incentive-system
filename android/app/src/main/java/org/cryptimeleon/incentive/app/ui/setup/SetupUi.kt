package org.cryptimeleon.incentive.app.ui.setup

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme

@Composable
fun SetupUi(setupViewModel: SetupViewModel, onFinished: () -> Unit) {
    val setupFinished by setupViewModel.navigateToInfo.observeAsState(false)

    LaunchedEffect(false) {
        setupViewModel.startSetup()
    }

    if (setupFinished) {
        LaunchedEffect(false) {
            onFinished()
        }
    }

    SetupUi()
}

@Composable
private fun SetupUi() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
fun SetupUiPreview() {
    CryptimeleonTheme {
        SetupUi()
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
