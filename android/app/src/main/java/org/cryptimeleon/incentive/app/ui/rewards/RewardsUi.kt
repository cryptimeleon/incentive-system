package org.cryptimeleon.incentive.app.ui.rewards

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ZkpTokenUpdate
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.ui.preview.PreviewData
import java.math.BigInteger

@Composable
fun RewardsUi(gotoCheckout: () -> Unit) {
    val rewardsViewModel = hiltViewModel<RewardsViewModel>()
    val promotionDataList by rewardsViewModel.promotionDataListFlow.collectAsState(initial = emptyList())

    RewardsUi(
        promotionDataList = promotionDataList,
        setUserUpdateChoice = rewardsViewModel::setUpdateChoice,
        gotoCheckout = gotoCheckout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardsUi(
    promotionDataList: List<PromotionData>,
    setUserUpdateChoice: (promotionId: BigInteger, tokenUpdate: TokenUpdate) -> Unit,
    gotoCheckout: () -> Unit
) {
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = { Text("Checkout: Choose Rewards") },
                menuEnabled = false
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(it)
                .padding(16.dp)
        ) {
            RewardPromotionList(
                promotionDataList = promotionDataList,
                setUserUpdateChoice = setUserUpdateChoice,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = gotoCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun RewardPromotionList(
    promotionDataList: List<PromotionData>,
    setUserUpdateChoice: (promotionId: BigInteger, tokenUpdate: TokenUpdate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        promotionDataList.forEach { promotion ->
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = promotion.promotionName,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            promotion.tokenUpdates
                .filter { it.isFeasible() }
                .forEach { tokenUpdate ->
                    item() {
                        RewardChoiceCard(tokenUpdate, promotion.pid, setUserUpdateChoice)
                    }
                }

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardChoiceCard(
    tokenUpdate: TokenUpdate,
    promotionId: BigInteger,
    setUserUpdateChoice: (promotionId: BigInteger, tokenUpdate: TokenUpdate) -> Unit
) {
    OutlinedCard(
        onClick = {
            setUserUpdateChoice(
                promotionId,
                tokenUpdate
            )
        },
        colors = if (tokenUpdate.isSelected()) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        // colors = if (choice.isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .defaultMinSize(minHeight = 100.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp), Arrangement.spacedBy(8.dp)) {
            Text(
                text = tokenUpdate.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (tokenUpdate is ZkpTokenUpdate && tokenUpdate.sideEffect.isPresent) {
                Row {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "Gift icon")
                    Text(
                        text = tokenUpdate.sideEffect.get(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
@Preview(
    showBackground = true,
    name = "BasketItem expanded"
)
fun BasketItemPreviewExpanded() {
    CryptimeleonTheme {
        RewardsUi(
            PreviewData.promotionDataList,
            setUserUpdateChoice = { _, _ -> },
            gotoCheckout = {}
        )
    }
}
