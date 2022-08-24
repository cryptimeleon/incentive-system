package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionUpdateFeasibility
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ZkpTokenUpdate
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData
import org.cryptimeleon.incentive.app.util.formatCents

@Composable
internal fun SummaryUi(
    basket: Basket,
    promotionData: List<PromotionData>,
    triggerCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LazyColumn(verticalArrangement = Arrangement.Top, modifier = Modifier.weight(1f)) {
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
            items(basket.items) { item ->
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
                                text = formatCents(item.price * item.count),
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
                    basket.items.sumOf { it.count },
                    basket.value
                )
            }
            item {
                Spacer(Modifier.size(16.dp))
            }
            if (promotionData.isNotEmpty()) {
                if (promotionData.none { p -> p.tokenUpdates.any { t -> t.feasibility == PromotionUpdateFeasibility.SELECTED && t !is NoTokenUpdate } }) {
                    item {
                        Text(
                            "No Rewards this time 😥",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                } else {
                    item {
                        TitleRowWithIcon("Rewards", Icons.Default.Redeem)
                    }
                    promotionData.forEach { promotionState ->
                        promotionState.tokenUpdates.find { t -> t.feasibility == PromotionUpdateFeasibility.SELECTED && t !is NoTokenUpdate }
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
            when {
                tokenUpdate is ZkpTokenUpdate && tokenUpdate.sideEffect.isPresent -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = "Gift icon"
                        )
                        Text(
                            text = tokenUpdate.sideEffect.get(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
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
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
private fun BasketSummaryRow(
    basketItemsCount: Int,
    basketValue: Int
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val pluralAppendix = if (basketItemsCount > 1) "s" else ""
        Text(
            text = "Total (${basketItemsCount} Item${pluralAppendix}): ",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = formatCents(basketValue),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
fun CheckoutUiNotStartedPreview() {
    CryptimeleonPreviewContainer {
        SummaryUi(
            basket = PreviewData.basket,
            promotionData = PreviewData.promotionDataList,
            triggerCheckout = {},
        )
    }
}
