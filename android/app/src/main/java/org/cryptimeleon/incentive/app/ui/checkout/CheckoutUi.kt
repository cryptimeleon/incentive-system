package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import java.util.*

@Composable
fun CheckoutUi(navigateHome: () -> Unit) {
    val checkoutViewModel = hiltViewModel<CheckoutViewModel>()
    val checkoutState: CheckoutState by checkoutViewModel.checkoutState.collectAsState(
        initial = CheckoutState(
            emptyList(),
            BasketState(0, "", emptyList())
        )
    )
    val payAndRedeemState: PayAndRedeemState by checkoutViewModel.payAndRedeemState.collectAsState(
        initial = PayAndRedeemState.NOT_STARTED
    )
    val paidBasketId: UUID? by checkoutViewModel.paidBasketId.collectAsState()

    val promotionDataCollection: List<PromotionData> by checkoutViewModel.promotionData.collectAsState(
        initial = emptyList()
    )

    CheckoutUi(
        promotionDataCollection,
        checkoutState,
        payAndRedeemState,
        paidBasketId,
        checkoutViewModel::startPayAndRedeem,
        navigateHome
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutUi(
    promotionDataCollection: List<PromotionData>,
    checkoutState: CheckoutState,
    payAndRedeemState: PayAndRedeemState,
    paidBasketId: UUID? = null,
    triggerCheckout: () -> Unit,
    navigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        Box(Modifier.padding(it)) {
            when (payAndRedeemState) {
                PayAndRedeemState.NOT_STARTED -> {
                    SummaryUi(checkoutState, triggerCheckout, promotionDataCollection)
                }
                PayAndRedeemState.FINISHED -> {
                    FinishedUi(checkoutState, paidBasketId, navigateHome)
                }
                else -> {
                    PayProgressUi(payAndRedeemState)
                }
            }
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


private val previewCheckoutState = CheckoutState(
    listOf(
        CheckoutPromotionState("First Promotion", "Become VIP Gold"),
        CheckoutPromotionState("Second Promotion", "Free Pan")
    ),
    BasketState(
        2500,
        UUID.randomUUID().toString(),
        listOf(
            BasketItem("Nutella", 2, 199, 398),
            BasketItem(
                "Apple", 5, 25, 125
            )
        )
    )
)

@Preview
@Composable
fun CheckoutUiInProgressPreview() {
    CryptimeleonTheme() {
        CheckoutUi(
            promotionDataCollection = emptyList(),
            checkoutState = previewCheckoutState,
            payAndRedeemState = PayAndRedeemState.UPDATE_TOKENS,
            triggerCheckout = {},
            navigateHome = {},
        )
    }
}
