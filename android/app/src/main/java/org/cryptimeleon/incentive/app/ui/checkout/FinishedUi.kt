package org.cryptimeleon.incentive.app.ui.checkout

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import timber.log.Timber
import java.util.*

@Composable
internal fun FinishedUi(
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
private fun generateBasketQRCode(
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

@Preview
@Composable
fun CheckoutUiFinishedPreview() {
    CryptimeleonTheme() {
        FinishedUi(
            checkoutState = CheckoutState(emptyList(), BasketState("", "", emptyList())),
            paidBasketId = null,
            navigateHome = {}
        )
    }
}
