package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer

@Composable
fun DSPreventedUi(
    navigateHome: () -> Unit = {},
    disableDoubleSpending: () -> Unit = {},
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
                "🧐",
                fontSize = 80.sp,
                modifier = Modifier.padding(32.dp)
            )
            Text(
                "You got caught!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                "Your attempt to double-spend has been detected and prevented!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        TextButton(onClick = navigateHome, Modifier.fillMaxWidth()) {
            Text("Navigate Home")
        }
        Button(onClick = disableDoubleSpending, Modifier.fillMaxWidth()) {
            Text("Disable Double-Spending")
        }
    }
}

@Preview
@Composable
fun DSPreventedUiPreview() {
    CryptimeleonPreviewContainer {
        DSPreventedUi()
    }
}
