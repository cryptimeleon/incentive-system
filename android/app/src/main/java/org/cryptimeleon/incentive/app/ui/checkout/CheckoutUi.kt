package org.cryptimeleon.incentive.app.ui.checkout

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
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
private fun FinishedUi(
    checkoutState: CheckoutState,
    paidBasketId: UUID?,
    navigateHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Success! 🎉",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.size(36.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp)

            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    val image by generateBasketQRCode(
                        basketId = paidBasketId!!.toString(),
                        fg = MaterialTheme.colorScheme.onSecondaryContainer,
                        bg = MaterialTheme.colorScheme.secondaryContainer,
                    )
                    Timber.i(image.toString())
                    image?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Basket id as QR code",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            filterQuality = FilterQuality.None,
                        )
                    }
                    Text(
                        checkoutState.basketState.basketId.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
            // TOOD show rewards to claim and QR code of basketId
        }
        Button(onClick = navigateHome, Modifier.fillMaxWidth()) {
            Text("Navigate Home")
        }
    }
}


@Composable
fun generateBasketQRCode(
    basketId: String,
    fg: Color,
    bg: Color
): State<ImageBitmap?> {
    val url = "incentives.cs.upb.de/basket/basket?basketId=${basketId}"
    Timber.i(url)

    return produceState<ImageBitmap?>(initialValue = null) {
        val qrCode = QRCodeWriter()
        val height = 192
        val width = 192
        try {
            Timber.i(System.currentTimeMillis().toString() + "endode url")
            val bitMatrix = qrCode.encode(url, BarcodeFormat.QR_CODE, width, height)

            Timber.i(System.currentTimeMillis().toString() + "create bitmap")
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)


            Timber.i(System.currentTimeMillis().toString() + "loops")
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) fg.toArgb() else bg.toArgb())
                }
            }
            Timber.i(System.currentTimeMillis().toString() + "as image")
            value = bitmap.asImageBitmap()
            Timber.i(System.currentTimeMillis().toString() + "done")
        } catch (e: WriterException) {
            Timber.e(e)
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

@Preview
@Composable
fun CheckoutUiNotStartedPreview() {
    CryptimeleonTheme() {
        CheckoutUi(
            checkoutState = previewCheckoutState,
            payAndRedeemState = PayAndRedeemState.NOT_STARTED,
            triggerCheckout = {},
            navigateHome = {},
        )
    }
}

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

@Preview
@Composable
fun CheckoutUiFinishedPreview() {
    CryptimeleonTheme() {
        CheckoutUi(
            checkoutState = previewCheckoutState,
            payAndRedeemState = PayAndRedeemState.FINISHED,
            triggerCheckout = {},
            navigateHome = {},
        )
    }
}
