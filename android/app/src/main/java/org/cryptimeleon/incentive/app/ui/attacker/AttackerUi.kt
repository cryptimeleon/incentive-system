package org.cryptimeleon.incentive.app.ui.attacker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences
import org.cryptimeleon.incentive.app.ui.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttackerUi(onUpClicked: () -> Unit) {

    val viewModel = hiltViewModel<AttackerViewModel>()
    val doubleSpendingAttackEnabled: DoubleSpendingPreferences by viewModel.doubleSpendingPreferencesFlow.collectAsState(
        DoubleSpendingPreferences(false)
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
                title = { Text("Attacker") },
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
        Box(Modifier.padding(contentPadding)) {
            AttackerUi(
                doubleSpendingAttackEnabled.discardUpdatedToken,
                viewModel::setDiscardUpdatedToken
            )
        }
    }
}

@Composable
private fun AttackerUi(
    doubleSpendingAttackEnabled: Boolean,
    setDiscardEnabled: (Boolean) -> Unit = {}
) {
    Column(Modifier.padding(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Text("🦹", style = MaterialTheme.typography.displayLarge)
        }
        Text(text = buildAnnotatedString {
            append("Since tokens are digitally stored on the phone, malicious actors can store a token, and spend it twice. ")
            append("This is called ")
            append(
                AnnotatedString("double-spending attack")
            )
            append(". You can perform a double-spending attack by activating the button below.")
            append("In this case, whenever you spend a token it is not overridden by the new one and can thus be spent again!")
        })
        Spacer(modifier = Modifier.size(32.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconToggleButton(
                checked = doubleSpendingAttackEnabled,
                onCheckedChange = setDiscardEnabled,
                Modifier.size(256.dp)
            ) {
                Icon(
                    Icons.Default.Bolt,
                    "Attack Icon",
                    Modifier.fillMaxSize()
                )
            }
            val buttonText =
                if (doubleSpendingAttackEnabled) "Disable Double-spending" else "Enable Double-spending"
            Text(buttonText, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
@Preview
private fun AttackerUiPreview() {
    CryptimeleonPreviewContainer {
        AttackerUi(true)
    }
}
