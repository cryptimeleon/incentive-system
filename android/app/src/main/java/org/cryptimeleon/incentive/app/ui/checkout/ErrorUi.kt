package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cryptimeleon.incentive.app.domain.PayRedeemException
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemStatus
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer

@Composable
fun ErrorUi(
    e: PayAndRedeemStatus.Error,
    deleteBasketAndGoHome: () -> Unit = {},
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
                "🤒",
                fontSize = 80.sp,
                modifier = Modifier.padding(32.dp)
            )
            Text(
                "An unexpected error occurred!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
            Text(
                if (e.e is PayRedeemException) "${e.e.code}: ${e.e.msg}" else "",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
        Button(onClick = deleteBasketAndGoHome, Modifier.fillMaxWidth()) {
            Text("Delete Basket and Navigate Home")
        }
    }
}

@Preview
@Composable
fun ErrorUiPreview() {
    CryptimeleonPreviewContainer {
        ErrorUi(PayAndRedeemStatus.Error(PayRedeemException(400, "Basket not Paid!")))
    }
}
