package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme

@Composable
internal fun SummaryUi(
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

@Preview
@Composable
fun CheckoutUiNotStartedPreview() {
    CryptimeleonTheme() {
        SummaryUi(
            checkoutState = CheckoutState(
                emptyList(),
                basketState = BasketState("", "", emptyList())
            ),
            triggerCheckout = {}
        )
    }
}
