package org.cryptimeleon.incentive.app.ui.checkout

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import timber.log.Timber
import java.util.*


@Composable
internal fun OnlyRewardClaimedUi(
    paidBasketId: UUID,
    paidBasketUrl: String,
    navigateHome: () -> Unit
) {
    FinishedUiWithQRCode("Reward claimed successfully without remainder token 🤖", paidBasketId, paidBasketUrl, navigateHome)
}

@Composable
internal fun FinishedUi(
    paidBasketId: UUID,
    paidBasketUrl: String,
    navigateHome: () -> Unit
) {
    FinishedUiWithQRCode("Success! 🎉", paidBasketId, paidBasketUrl, navigateHome)
}

@Composable
private fun FinishedUiWithQRCode(message: String, paidBasketId: UUID, paidBasketUrl: String, navigateHome: () -> Unit) {
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
                message,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Scan this QR code when leaving the store",
                style = MaterialTheme.typography.bodyMedium,
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
                        basketUrl = paidBasketUrl,
                        fg = MaterialTheme.colorScheme.onSecondaryContainer,
                        bg = MaterialTheme.colorScheme.secondaryContainer,
                    )
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
                        paidBasketId.toString().uppercase(Locale.getDefault()),
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

/**
 * Create compose-compatible QR code that encodes url of basket
 */
@Composable
private fun generateBasketQRCode(
    basketUrl: String,
    fg: Color,
    bg: Color
): State<ImageBitmap?> {
    return produceState<ImageBitmap?>(initialValue = null) {
        val qrCode = QRCodeWriter()
        val height = 192
        val width = 192
        try {
            val bitMatrix = qrCode.encode(basketUrl, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) fg.toArgb() else bg.toArgb())
                }
            }
            value = bitmap.asImageBitmap()
        } catch (e: WriterException) {
            Timber.e(e)
        }
    }
}

@Preview
@Composable
fun CheckoutUiFinishedPreview() {
    CryptimeleonPreviewContainer {
        FinishedUi(
            paidBasketId = UUID.randomUUID(),
            paidBasketUrl = "https://basket.id",
            navigateHome = {}
        )
    }
}
