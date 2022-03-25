package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar

@Composable
fun CheckoutUi() {
    val checkoutViewModel = hiltViewModel<CheckoutViewModel>()
    val payAndRedeemState by checkoutViewModel.payAndRedeemState.collectAsState()

    Scaffold(topBar = {
        DefaultTopAppBar(
            title = { Text(text = "Checkout") },
            menuEnabled = false,
            // TODO add back button?
        )
    }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Summary")
            Text("TODO: show all infos")
            Button(onClick = { checkoutViewModel.startPayAndRedeem() }) {
                Text("Pay and Redeem")
            }
            Text(payAndRedeemState.name)
        }
    }
}


@Preview
@Composable
fun CheckoutUiPreview() {
    CheckoutUi()
}
