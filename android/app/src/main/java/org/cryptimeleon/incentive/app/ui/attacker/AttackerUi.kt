package org.cryptimeleon.incentive.app.ui.attacker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences
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
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Double-spending Attack")
                    IconToggleButton(
                        checked = doubleSpendingAttackEnabled.discardUpdatedToken,
                        onCheckedChange = viewModel::setDiscardUpdatedToken
                    ) {
                        Icon(Icons.Default.Bolt, "Attack Icon")
                    }
                }
            }
        }
    }
}
