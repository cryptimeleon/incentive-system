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
import org.cryptimeleon.incentive.app.domain.model.Earn
import org.cryptimeleon.incentive.app.domain.model.None
import org.cryptimeleon.incentive.app.domain.model.UserUpdateChoice
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import java.math.BigInteger

@Composable
fun RewardsUi(gotoCheckout: () -> Unit) {
    val rewardsViewModel = hiltViewModel<RewardsViewModel>()
    val state: RewardsState by rewardsViewModel.state.collectAsState(
        initial = RewardsState(emptyList())
    )
    RewardsUi(
        state = state,
        setUserUpdateChoice = rewardsViewModel::setUpdateChoice,
        gotoCheckout = gotoCheckout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardsUi(
    state: RewardsState,
    setUserUpdateChoice: (promotionId: BigInteger, userUpdateChoice: UserUpdateChoice) -> Unit,
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
                state.promotionInfos,
                setUserUpdateChoice = setUserUpdateChoice,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = gotoCheckout, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RewardPromotionList(
    promotionInfos: List<PromotionInfo>,
    setUserUpdateChoice: (promotionId: BigInteger, userUpdateChoice: UserUpdateChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        promotionInfos.forEach { promotion ->
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = promotion.promotionName,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            promotion.choices.forEach { choice ->
                item() {
                    RewardChoiceCard(setUserUpdateChoice, promotion, choice)
                }
            }

        }
    }

}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RewardChoiceCard(
    setUserUpdateChoice: (promotionId: BigInteger, userUpdateChoice: UserUpdateChoice) -> Unit,
    promotion: PromotionInfo,
    choice: Choice
) {
    OutlinedCard(
        onClick = {
            setUserUpdateChoice(
                promotion.promotionId,
                choice.userUpdateChoice
            )
        },
        colors = if (choice.isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        // colors = if (choice.isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .defaultMinSize(minHeight = 100.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp), Arrangement.spacedBy(8.dp)) {
            Text(
                text = choice.humanReadableDescription,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            when (val sideEffect = choice.sideEffect) {
                is RewardChoiceSideEffect -> Row {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "Gift icon")
                    Text(
                        text = sideEffect.title,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                is NoChoiceSideEffect -> {}
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
            state = RewardsState(
                listOf(
                    PromotionInfo(
                        BigInteger.valueOf(1),
                        "Promotion for Preview",
                        listOf(
                            Choice(
                                "Nothing",
                                "No cryptographic protocols are executed. The token remains unchanged.",
                                NoChoiceSideEffect,
                                None,
                                true
                            ),
                            Choice(
                                "Collect 10 points",
                                "Use the fast-earn protocol to add [10 0] to the points vector of the token and update the SPSEQ signature accordingly",
                                NoChoiceSideEffect,
                                Earn,
                                false
                            ),
                            Choice(
                                "Get Teddy and collect 3 points",
                                "Run the ZKP with id 0237452398 to get change the points vector from [32] to [35].",
                                RewardChoiceSideEffect("2897345987397", "Teddy Bear"),
                                Earn,
                                false
                            )
                        )

                    )
                )
            ),
            setUserUpdateChoice = { _, _ -> },
            gotoCheckout = {}
        )
    }
}
