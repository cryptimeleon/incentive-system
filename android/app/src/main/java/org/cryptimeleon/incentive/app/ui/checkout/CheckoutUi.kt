package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemStatus
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
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
fun CheckoutUi(navigateHome: () -> Unit, navigateToLoadingScreen: () -> Unit) {
    val checkoutViewModel = hiltViewModel<CheckoutViewModel>()

    val basket by checkoutViewModel.basket.collectAsState(initial = null)
    val promotionDataCollection: List<PromotionData> by checkoutViewModel.promotionData.collectAsState(
        initial = emptyList()
    )
    // Once a basket is paid and new tokens are retrieved, it is removed from the database.
    // Therefore, we need to store the old ID
    val returnCode: PayAndRedeemStatus? by checkoutViewModel.returnCode.collectAsState()

    val checkoutStep: CheckoutStep by checkoutViewModel.checkoutStep.collectAsState()

    CheckoutUi(
        basket,
        promotionDataCollection,
        checkoutStep,
        returnCode,
        checkoutViewModel::startPayAndRedeem,
        checkoutViewModel::deleteBasket,
        checkoutViewModel::disableDSAndRecover,
        navigateHome,
        navigateToLoadingScreen
    )
}

@Composable
private fun CheckoutUi(
    basket: Basket?,
    promotionDataCollection: List<PromotionData>,
    checkoutStep: CheckoutStep,
    status: PayAndRedeemStatus? = null,
    triggerCheckout: () -> Unit,
    deleteBasket: () -> Unit,
    disableDsAndRecover: () -> Unit,
    navigateHome: () -> Unit,
    navigateToLoadingScreen: () -> Unit
) {
    val title = when (checkoutStep) {
        CheckoutStep.SUMMARY -> "Summary"
        CheckoutStep.PROCESSING -> "Processing"
        CheckoutStep.FINISHED -> "Finished"
    }
    Scaffold(
        topBar = { DefaultTopAppBar(title = { Text("Checkout: $title") }, menuEnabled = false) },
    ) {
        Box(Modifier.padding(it)) {
            when (checkoutStep) {
                CheckoutStep.SUMMARY -> {
                    basket?.let { // Should not be null in this case, but can be in finished case!
                        SummaryUi(basket, promotionDataCollection, triggerCheckout)
                    }
                }
                CheckoutStep.FINISHED -> {
                    when (status) {
                        is PayAndRedeemStatus.Success -> FinishedUi(
                            status.basketId,
                            status.basketUrl,
                            navigateHome
                        )
                        is PayAndRedeemStatus.DSStopAfterCLaimingReward -> OnlyRewardClaimedUi(
                            status.basketId,
                            status.basketUrl,
                            navigateHome
                        )
                        is PayAndRedeemStatus.DSDetected -> DSPreventedUi(
                            stepDetected = status.step,
                            navigateHome = navigateHome,
                            disableDoubleSpending = {
                                disableDsAndRecover()
                                navigateToLoadingScreen()
                            },
                        )
                        is PayAndRedeemStatus.Error -> ErrorUi(
                            e = status,
                            deleteBasketAndGoHome = {
                                deleteBasket()
                                navigateHome()
                            }
                        )
                        else -> {}
                    }
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
                delay(2000L)
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
