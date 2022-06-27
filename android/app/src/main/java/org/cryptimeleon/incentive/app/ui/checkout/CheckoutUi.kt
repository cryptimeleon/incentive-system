package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import java.util.*

@Composable
fun CheckoutUi(navigateHome: () -> Unit) {
    val checkoutViewModel = hiltViewModel<CheckoutViewModel>()
    val checkoutState: CheckoutState by checkoutViewModel.checkoutState.collectAsState(
        initial = CheckoutState(
            emptyList(),
            BasketState("", "", emptyList())
        )
    )
    val payAndRedeemState: PayAndRedeemState by checkoutViewModel.payAndRedeemState.collectAsState(
        initial = PayAndRedeemState.NOT_STARTED
    )

    CheckoutUi(
        checkoutState,
        payAndRedeemState,
        checkoutViewModel::startPayAndRedeem,
        navigateHome
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutUi(
    checkoutState: CheckoutState,
    payAndRedeemState: PayAndRedeemState,
    triggerCheckout: () -> Unit,
    navigateHome: () -> Unit,
) {
    Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        Box(Modifier.padding(it)) {
            when (payAndRedeemState) {
                PayAndRedeemState.NOT_STARTED -> {
                    SummaryUi(checkoutState, triggerCheckout)
                }
                PayAndRedeemState.FINISHED -> {
                    FinishedUi(checkoutState, navigateHome)
                }
                else -> {
                    PayProgressUi(payAndRedeemState)
                }
            }
        }
    }
}

@Composable
private fun SummaryUi(
    checkoutState: CheckoutState,
    triggerCheckout: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LazyColumn {
            item {
                Text(
                    "Summary",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                Text(
                    "Basket:",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(checkoutState.basketState.basketItems) { basketItem ->
                Row(Modifier.fillMaxWidth()) {
                    Text(basketItem.title, modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(basketItem.costTotal)
                        Row {
                            Text("${basketItem.count}x${basketItem.costSingle}")
                        }
                    }
                }
            }
            item {
                Divider(Modifier.padding(vertical = 8.dp))
            }
            item {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        "Total:",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        checkoutState.basketState.basketValue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            item {
                Spacer(Modifier.size(16.dp))
            }
            item {
                Text(
                    "Chosen Promotion Updates:",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            items(checkoutState.promotionStates) { promotionState ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(promotionState.promotionName)
                    Text(promotionState.choiceDescription)
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
        Button(onClick = triggerCheckout, Modifier.fillMaxWidth()) {
            Text("Pay and Redeem")
        }
    }
}

@Composable
private fun PayProgressUi(payAndRedeemState: PayAndRedeemState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            payAndRedeemState.toString(),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun FinishedUi(checkoutState: CheckoutState, navigateHome: () -> Unit) {
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
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.size(16.dp))
            // TODO replace by QR code
            Text(
                "Your basket id for claiming rewards is: ${checkoutState.basketState.basketId}",
                style = MaterialTheme.typography.headlineSmall
            )
            // TOOD show rewards to claim and QR code of basketId
        }
        Button(onClick = navigateHome, Modifier.fillMaxWidth()) {
            Text("Navigate Home")
        }
    }
}


private val previewCheckoutState = CheckoutState(
    listOf(
        CheckoutPromotionState("First Promotion", "Become VIP Gold"),
        CheckoutPromotionState("Second Promotion", "Free Pan")
    ),
    BasketState(
        "25,00€",
        UUID.randomUUID().toString(),
        listOf(
            BasketItem("Nutella", 2, "1,99€", "3,98€"),
            BasketItem(
                "Apple", 5, "0,25€", "1,25€"
            )
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CheckoutUiNotStartedPreview() {
    CryptimeleonTheme() {
        Scaffold() {
            CheckoutUi(
                checkoutState = previewCheckoutState,
                payAndRedeemState = PayAndRedeemState.NOT_STARTED,
                triggerCheckout = {},
                navigateHome = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CheckoutUiInProgressPreview() {
    CryptimeleonTheme() {
        Scaffold() {
            CheckoutUi(
                checkoutState = previewCheckoutState,
                payAndRedeemState = PayAndRedeemState.UPDATE_TOKENS,
                triggerCheckout = {},
                navigateHome = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CheckoutUiFinishedPreview() {
    CryptimeleonTheme() {
        Scaffold() {
            CheckoutUi(
                checkoutState = previewCheckoutState,
                payAndRedeemState = PayAndRedeemState.FINISHED,
                triggerCheckout = {},
                navigateHome = {},
            )
        }
    }
}
