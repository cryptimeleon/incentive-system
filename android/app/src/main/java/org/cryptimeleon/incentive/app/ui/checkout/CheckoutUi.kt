package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import java.math.BigInteger
import java.util.*

val checkoutTexts = arrayOf(
    "Your data is protected by cryptography",
    "Leveraging zero-knowledge proofs to keep your private data safe",
    "Fiat-Shamir-transforming the zero-knowledge proofs to speedup the checkout",
    "We value your privacy by only disclosing necessary data",
    "Our code is 100% open-source",
    "Privacy and transparency first!",
)

@Composable
fun CheckoutUi(navigateHome: () -> Unit) {
    val checkoutViewModel = hiltViewModel<CheckoutViewModel>()

    val basket by checkoutViewModel.basket.collectAsState(initial = null)
    val promotionDataCollection: List<PromotionData> by checkoutViewModel.promotionData.collectAsState(
        initial = emptyList()
    )
    // Once a basket is paid and new tokens are retrieved, it is removed from the database.
    // Therefore, we need to store the old ID
    val paidBasketId: UUID? by checkoutViewModel.paidBasketId.collectAsState()

    val checkoutStep: CheckoutStep by checkoutViewModel.checkoutStep.collectAsState()

    CheckoutUi(
        basket,
        promotionDataCollection,
        checkoutStep,
        paidBasketId,
        checkoutViewModel::gotoSummary,
        checkoutViewModel::setUpdateChoice,
        checkoutViewModel::startPayAndRedeem,
        navigateHome
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutUi(
    basket: Basket?,
    promotionDataCollection: List<PromotionData>,
    checkoutStep: CheckoutStep,
    paidBasketId: UUID? = null,
    gotoSummary: () -> Unit,
    setUserUpdateChoice: (BigInteger, TokenUpdate) -> Unit,
    triggerCheckout: () -> Unit,
    navigateHome: () -> Unit,
) {
    val title = when (checkoutStep) {
        CheckoutStep.REWARDS -> "Rewards"
        CheckoutStep.SUMMARY -> "Summary"
        CheckoutStep.PROCESSING -> "Processing"
        CheckoutStep.FINISHED -> "Finished"
    }
    Scaffold(
        topBar = { DefaultTopAppBar(title = { Text("Checkout: $title") }, menuEnabled = false) },
    ) {
        Box(Modifier.padding(it)) {
            when (checkoutStep) {
                CheckoutStep.REWARDS -> {
                    RewardsUi(
                        promotionDataCollection,
                        setUserUpdateChoice,
                        gotoSummary,
                    )
                }
                CheckoutStep.SUMMARY -> {
                    basket?.let { // Should not be null in this case, but can be in finished case!
                        SummaryUi(basket, promotionDataCollection, triggerCheckout)
                    }
                }
                CheckoutStep.FINISHED -> {
                    FinishedUi(paidBasketId, navigateHome)
                }
                CheckoutStep.PROCESSING -> {
                    PayProgressUi()
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PayProgressUi() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(strokeWidth = 4.dp, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.size(32.dp))

        val target by produceState(initialValue = checkoutTexts.random()) {
            while (true) {
                delay(1200L)
                value = checkoutTexts.random()
            }
        }

        AnimatedContent(
            targetState = target,
        ) { str ->
            Text(
                text = str,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview
@Composable
fun CheckoutUiInProgressPreview() {
    CryptimeleonPreviewContainer {
        PayProgressUi()
    }
}
