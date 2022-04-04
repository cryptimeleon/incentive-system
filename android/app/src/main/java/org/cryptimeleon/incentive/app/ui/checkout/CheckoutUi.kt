package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar

@Composable
fun CheckoutUi(navigateHome: () -> Unit) {
    val checkoutViewModel = hiltViewModel<CheckoutViewModel>()
    val checkoutState: CheckoutState by checkoutViewModel.checkoutState.collectAsState(
        initial = CheckoutState(
            PayAndRedeemState.NOT_STARTED,
            emptyList()
        )
    )

    CheckoutUi(
        checkoutState,
        checkoutViewModel::startPayAndRedeem,
        navigateHome
    )
}

@Composable
private fun CheckoutUi(
    checkoutState: CheckoutState,
    triggerCheckout: () -> Unit,
    navigateHome: () -> Unit,
) {
    Scaffold(topBar = {
        DefaultTopAppBar(
            title = { Text(text = "Checkout") },
            menuEnabled = false,
            // TODO add back button?
        )
    }) {
        when (checkoutState.payAndRedeemState) {
            PayAndRedeemState.NOT_STARTED -> {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    LazyColumn {
                        items(checkoutState.promotionStates) { promotionState ->
                            Text(promotionState.promotionName, style = MaterialTheme.typography.h5)
                            Text(promotionState.choiceDescription)
                        }
                    }
                    Button(onClick = triggerCheckout, Modifier.fillMaxWidth()) {
                        Text("Pay and Redeem")
                    }
                }
            }
            PayAndRedeemState.FINISHED -> {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Successfully updated tokens!",
                            style = MaterialTheme.typography.h5
                        )
                    }
                    Button(onClick = navigateHome, Modifier.fillMaxWidth()) {
                        Text("Navigate Home")
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        checkoutState.payAndRedeemState.toString(),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun CheckoutUiNotStartedPreview() {
    Scaffold() {
        CheckoutUi(
            checkoutState = CheckoutState(
                PayAndRedeemState.NOT_STARTED,
                listOf(
                    CheckoutPromotionState("First Promotion", "Become VIP Gold"),
                    CheckoutPromotionState("Second Promotion", "Free Pan")
                )
            ),
            triggerCheckout = {},
            navigateHome = {},
        )
    }
}

@Preview
@Composable
fun CheckoutUiInProgressPreview() {
    Scaffold() {
        CheckoutUi(
            checkoutState = CheckoutState(
                PayAndRedeemState.PAY,
                listOf(
                    CheckoutPromotionState("First Promotion", "Become VIP Gold"),
                    CheckoutPromotionState("Second Promotion", "Free Pan")
                )
            ),
            triggerCheckout = {},
            navigateHome = {},
        )
    }
}

@Preview
@Composable
fun CheckoutUiFinishedPreview() {
    Scaffold() {
        CheckoutUi(
            checkoutState = CheckoutState(
                PayAndRedeemState.FINISHED,
                listOf(
                    CheckoutPromotionState("First Promotion", "Become VIP Gold"),
                    CheckoutPromotionState("Second Promotion", "Free Pan")
                )
            ),
            triggerCheckout = {},
            navigateHome = {},
        )
    }
}
