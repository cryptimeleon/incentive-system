package org.cryptimeleon.incentive.app.ui.attacker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer

@Composable
fun AttackerUi(onUpClicked: () -> Unit) {

    val viewModel = hiltViewModel<AttackerViewModel>()
    val dsPreferences: DoubleSpendingPreferences by viewModel.doubleSpendingPreferencesFlow.collectAsState(
        DoubleSpendingPreferences(discardUpdatedToken = false, stopAfterPay = false)
    )

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
                title = { Text("Double-Spending Attack") },
                navigationIcon = {
                    IconButton(onClick = onUpClicked) {
                        Icon(Icons.Filled.ArrowBack, "Up Icon")
                    }
                },
                menuEnabled = false,
            )
        }
    ) { contentPadding ->
        // We apply the contentPadding passed to us from the Scaffold
        AttackerUi(
            dsPreferences.discardUpdatedToken,
            dsPreferences.stopAfterPay,
            Modifier.padding(contentPadding),
            viewModel::setDiscardUpdatedToken,
            viewModel::setStopAfterPayment
        )
    }
}

@Composable
private fun AttackerUi(
    discardTokenEnabled: Boolean,
    stopAfterPayment: Boolean,
    modifier: Modifier = Modifier,
    setDiscardEnabled: (Boolean) -> Unit = {},
    setStopAfterPayment: (Boolean) -> Unit = {},
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Text("📡", fontSize = 24.em)
        }
        DoubleSpendingText()
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Default.Delete,
                    "Discard Icon",
                    Modifier
                        .size(32.dp)
                )
                Text("Discard Remainder Token", style = MaterialTheme.typography.titleLarge)
            }
            Switch(checked = discardTokenEnabled, onCheckedChange = setDiscardEnabled)
        }
        Text(
            "Further, you can stop the protocol after the payment at the store and thus not communicate with the provider to obtain a remainder token." +
                    "This corresponds to the attack scenario where an attacker copies a token to two phones and tries to claim two rewards at two different stores."
        )
        Spacer(
            modifier = Modifier
                .height(8.dp)
                .fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Default.Storefront,
                    "Store Icon",
                    Modifier
                        .size(32.dp)
                )
                Text("Stop After Payment", style = MaterialTheme.typography.titleLarge)
            }
            Switch(checked = stopAfterPayment, onCheckedChange = setStopAfterPayment)
        }
        DoubleSpendingProtectionText(Modifier.padding(bottom = 32.dp, top = 16.dp))
    }
}

@Composable
private fun DoubleSpendingText() {
    Text(
        "What is double-spending?",
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(
        modifier = Modifier
            .height(8.dp)
            .fillMaxWidth()
    )
    Text(text = buildAnnotatedString {
        append("Tokens are digital and stored on the phone. Hence, malicious actors can copy a token and spend it multiple times. ")
        append("We say, they perform a ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append("double-spending attack")
        }
        append(".")
    })
    Spacer(
        modifier = Modifier
            .height(16.dp)
            .fillMaxWidth()
    )
    Text(
        "You can try these attacks by activating the double-spending mode below. " +
                "It allows you to use the same token multiple times by discarding the remainder token. "
    )
    Spacer(
        modifier = Modifier
            .height(8.dp)
            .fillMaxWidth()
    )
}

@Composable
private fun DoubleSpendingProtectionText(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            "How does the Incentive-System protect against double-spending?",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(
            modifier = Modifier
                .height(8.dp)
                .fillMaxWidth()
        )
        Text(text = buildAnnotatedString {
            append(
                "Whenever a malicious actor performs a double-spending attack, it leaks enough " +
                        "information for the double-spending protection mechanisms to extract the malicious " +
                        "actor's secret key which leaks the attacker's identity."
            )
        })
    }
}

@Composable
@Preview
private fun AttackerUiPreview() {
    CryptimeleonPreviewContainer {
        AttackerUi(true,true)
    }
}
