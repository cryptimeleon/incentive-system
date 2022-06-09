package org.cryptimeleon.incentive.app.ui.rewards

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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

@Composable
private fun RewardsUi(
    state: RewardsState,
    setUserUpdateChoice: (promotionId: BigInteger, userUpdateChoice: UserUpdateChoice) -> Unit,
    gotoCheckout: () -> Unit
) {
    Scaffold(topBar = {
        DefaultTopAppBar(
            title = { Text("Checkout - Rewards") },
            menuEnabled = false
        )
    }) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            RewardPromotionList(
                state.promotionInfos,
                setUserUpdateChoice = setUserUpdateChoice,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = gotoCheckout, modifier = Modifier.fillMaxWidth()) {
                Text("Summary")
            }
        }
    }
}

@Composable
fun RewardPromotionList(
    promotionInfos: List<PromotionInfo>,
    setUserUpdateChoice: (promotionId: BigInteger, userUpdateChoice: UserUpdateChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Choose a Reward for every Promotion", style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(promotionInfos) { promotion ->
            Card() {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = promotion.promotionName, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(Modifier.selectableGroup()) {
                        promotion.choices.forEach { choice ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = choice.isSelected,
                                        onClick = {
                                            setUserUpdateChoice(
                                                promotion.promotionId,
                                                choice.userUpdateChoice
                                            )
                                        },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = choice.isSelected,
                                    onClick = null // Recommended since Row already handles clicks
                                )
                                Column() {
                                    Text(
                                        text = choice.humanReadableDescription,
                                        style = MaterialTheme.typography.body1.merge(),
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                    when (val sideEffect = choice.sideEffect) {
                                        is RewardChoiceSideEffect -> Text(
                                            text = "Reward Item: ${sideEffect.title}",
                                            style = MaterialTheme.typography.body1.merge(),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                        is NoChoiceSideEffect -> {}
                                    }
                                    Text(
                                        text = choice.cryptographicDescription,
                                        style = MaterialTheme.typography.subtitle2,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
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
