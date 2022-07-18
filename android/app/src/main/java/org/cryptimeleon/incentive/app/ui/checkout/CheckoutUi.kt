package org.cryptimeleon.incentive.app.ui.checkout

import android.graphics.Bitmap
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import timber.log.Timber
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
    val paidBasketId: UUID? by checkoutViewModel.paidBasketId.collectAsState()

    val promotionData: List<PromotionData> by checkoutViewModel.promotionData.collectAsState(
        initial = emptyList()
    )

    CheckoutUi(
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
                    SummaryUi(checkoutState, triggerCheckout)
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

@Preview
@Composable
fun CheckoutUiInProgressPreview() {
    CryptimeleonTheme() {
        CheckoutUi(
            checkoutState = previewCheckoutState,
            payAndRedeemState = PayAndRedeemState.UPDATE_TOKENS,
            triggerCheckout = {},
            navigateHome = {},
        )
    }
}
