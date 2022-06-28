package org.cryptimeleon.incentive.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(onUpClicked: () -> Unit) {
    val viewModel = hiltViewModel<SettingsViewModel>()
    val pp by viewModel.publicParameter.collectAsState()
    val ppk by viewModel.providerPublicKey.collectAsState()
    val usk by viewModel.userSecretKey.collectAsState()
    val upk by viewModel.userPublicKey.collectAsState()
    val tokens by viewModel.tokens.collectAsState()

    Scaffold(
        bottomBar = {
            Spacer(
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .fillMaxWidth()
            )
        },
        topBar = {
            DefaultTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onUpClicked) {
                        Icon(Icons.Filled.ArrowBack, "Up Icon")
                    }
                },
                menuEnabled = false
            )
        }
    ) { contentPadding ->
        // We apply the contentPadding passed to us from the Scaffold
        Box(Modifier.padding(contentPadding)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CryptoItem(
                    "Public Parameters",
                    pp,
                )
                CryptoItem(
                    "Provider Public Key",
                    ppk,
                )
                CryptoItem(
                    "User Public Key",
                    upk,
                )
                CryptoItem(
                    "User Secret Key",
                    usk,
                )
                tokens.forEachIndexed { index, token ->
                    CryptoItem(
                        "Token ${index + 1}",
                        token,
                    )
                }
            }
        }
    }
}

@Composable
fun CryptoItem(title: String, info: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall
        )
        if (info == "") {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Text(
                info,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
