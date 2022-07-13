package org.cryptimeleon.incentive.app.ui.checkout

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.cryptimeleon.incentive.app.domain.usecase.Earn
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.None
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionUpdateFeasibility
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ZKPUpdate
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.basket.BasketSummaryRow
import org.cryptimeleon.incentive.app.ui.basket.formatCents
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.math.structures.cartesian.Vector
import timber.log.Timber
import java.math.BigInteger
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

    val promotionData: List<PromotionData> by checkoutViewModel.promotionData.collectAsState(
        initial = emptyList()
    )

    CheckoutUi(
        checkoutState,
        payAndRedeemState,
        paidBasketId,
        promotionData,
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
    promotionData: List<PromotionData> = emptyList(),
    triggerCheckout: () -> Unit,
    navigateHome: () -> Unit,
) {
    val title = when (payAndRedeemState) {
        PayAndRedeemState.NOT_STARTED -> "Summary"
        PayAndRedeemState.FINISHED -> "Finished"
        else -> "Processing"
    }
    Scaffold(
        topBar = { DefaultTopAppBar(title = { Text("Checkout: $title") }, menuEnabled = false) },
    ) {
        Box(Modifier.padding(it)) {
            when (payAndRedeemState) {
                PayAndRedeemState.NOT_STARTED -> {
                    SummaryUi(checkoutState, triggerCheckout, promotionData)
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
    triggerCheckout: () -> Unit,
    promotionData: List<PromotionData>,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LazyColumn(verticalArrangement = Arrangement.Top) {
            item {
                TitleRowWithIcon("Basket", Icons.Default.ShoppingBasket)
            }
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    Text(
                        "Price",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.6F
                        )
                    )
                }
            }
            items(checkoutState.basketState.basketItems) { item ->
                Divider()
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${item.count} x ${item.title}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCents(item.costSingle * item.count),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .align(Alignment.End)
                            )
                        }
                    }
                }
            }
            item {
                Divider()
            }
            item {
                BasketSummaryRow(
                    checkoutState.basketState.basketItems.sumOf { it.count },
                    checkoutState.basketState.basketValue
                )
            }
            item {
                Spacer(Modifier.size(16.dp))
            }
            item {
                TitleRowWithIcon("Rewards", Icons.Default.Redeem)
            }
            promotionData.forEach { promotionState ->
                promotionState.tokenUpdates.find { t -> t.feasibility == PromotionUpdateFeasibility.SELECTED && (t is ZKPUpdate || t is Earn) }
                    ?.let { tokenUpdate ->
                        item {
                            PromotionUpdateSummaryCard(
                                promotionState,
                                tokenUpdate,
                                Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
        Button(onClick = triggerCheckout, Modifier.fillMaxWidth()) {
            Text("Pay and Redeem")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromotionUpdateSummaryCard(
    promotionState: PromotionData,
    tokenUpdate: TokenUpdate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = 100.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = promotionState.promotionName,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = tokenUpdate.description,
                style = MaterialTheme.typography.bodyLarge,
            )
            when (tokenUpdate) {
                is ZKPUpdate ->
                    Row {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = "Gift icon"
                        )
                        Text(
                            text = tokenUpdate.sideEffect,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
            }
        }
    }
}

@Composable
private fun TitleRowWithIcon(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, contentDescription = null,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun PayProgressUi(payAndRedeemState: PayAndRedeemState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
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
    navigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
        2500,
        UUID.randomUUID().toString(),
        listOf(
            BasketItem("Nutella", 2, 199, 398),
            BasketItem("Apple", 5, 25, 125)
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CheckoutUiNotStartedPreview() {
    CryptimeleonTheme() {
        Scaffold() {
            SummaryUi(
                checkoutState = previewCheckoutState,
                promotionData = listOf(
                    HazelPromotionData(
                        "Nutella Promotion",
                        BigInteger.valueOf(5345L),
                        "Earn points for buying Nutella!",
                        Vector.of(BigInteger.valueOf(6L)),
                        listOf(
                            None(),
                            Earn(PromotionUpdateFeasibility.CANDIDATE),
                            HazelTokenUpdateState(
                                "Get a free glass of Nutella",
                                "Free Nutella",
                                feasibility = PromotionUpdateFeasibility.SELECTED,
                                6,
                                4
                            )
                        )
                    )
                ),
                triggerCheckout = {},
                modifier = Modifier.padding(it)
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
            PayProgressUi(
                payAndRedeemState = PayAndRedeemState.UPDATE_TOKENS,
                modifier = Modifier.padding(it)
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
            FinishedUi(
                checkoutState = previewCheckoutState,
                navigateHome = {},
                paidBasketId = UUID.randomUUID(),
                modifier = Modifier.padding(it)
            )
        }
    }
}
