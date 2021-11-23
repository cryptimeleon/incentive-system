package org.cryptimeleon.incentive.app.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Settings(onUpClicked: () -> Unit) {
    val viewModel = hiltViewModel<SettingsViewModel>()
    val pp by viewModel.publicParameter.observeAsState("")
    val ppk by viewModel.providerPublicKey.observeAsState("")
    val usk by viewModel.userSecretKey.observeAsState("")
    val upk by viewModel.userPublicKey.observeAsState("")
    val token by viewModel.token.observeAsState("")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onUpClicked) {
                        Icon(Icons.Filled.ArrowBack, "Up Icon")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
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
            CryptoItem(
                "Token",
                token,
            )
        }
    }
}

@Composable
fun CryptoItem(title: String, info: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.h5
        )
        Text(
            info,
            style = MaterialTheme.typography.body2
        )
    }
}