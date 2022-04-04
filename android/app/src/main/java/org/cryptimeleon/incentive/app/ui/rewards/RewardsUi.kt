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
import org.cryptimeleon.incentive.app.domain.model.PromotionState
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.UpdateChoice
import org.cryptimeleon.incentive.app.domain.model.UserUpdateChoice
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.promotion.EmptyTokenUpdateMetadata
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger
import java.util.*

@Composable
fun RewardsUi(gotoCheckout: () -> Unit) {
    val rewardsViewModel = hiltViewModel<RewardsViewModel>()
    val promotionStates: List<PromotionState> by rewardsViewModel.promotionStates.collectAsState(
        initial = emptyList()
    )
    val userUpdateChoices: List<PromotionUserUpdateChoice> by rewardsViewModel.tokenUpdateChoices.collectAsState(
        initial = emptyList()
    )

    RewardsUi(
        promotionStates = promotionStates,
        userTokenUpdateChoices = userUpdateChoices,
        setUserUpdateChoice = rewardsViewModel::setUpdateChoice,
        gotoCheckout = gotoCheckout,
    )
}

@Composable
private fun RewardsUi(
    promotionStates: List<PromotionState>,
    userTokenUpdateChoices: List<PromotionUserUpdateChoice>,
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
                promotionStates = promotionStates,
                userTokenUpdateChoices = userTokenUpdateChoices,
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
    promotionStates: List<PromotionState>,
    userTokenUpdateChoices: List<PromotionUserUpdateChoice>,
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
        items(promotionStates) {
            val selectedTokenUpdateChoice =
                userTokenUpdateChoices.find { choice -> choice.promotionId == it.promotionId }?.userUpdateChoice
            Card() {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = it.promotionName, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(Modifier.selectableGroup()) {
                        it.qualifiedUpdates.forEach { updateChoice ->
                            val isSelected =
                                (updateChoice.toUserUpdateChoice() == selectedTokenUpdateChoice)
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            setUserUpdateChoice(
                                                it.promotionId,
                                                updateChoice.toUserUpdateChoice()
                                            )
                                        },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (updateChoice.toUserUpdateChoice() == selectedTokenUpdateChoice),
                                    onClick = null // Recommended since Row already handles clicks
                                )
                                Text(
                                    text = updateChoice.toString(),
                                    style = MaterialTheme.typography.body1.merge(),
                                    modifier = Modifier.padding(start = 16.dp)
                                )
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
    RewardsUi(
        promotionStates = listOf(
            PromotionState(
                BigInteger.valueOf(1),
                "Promotion for Preview",
                Vector.of(BigInteger.valueOf(45)),
                Vector.of(BigInteger.valueOf(3)),
                listOf(
                    UpdateChoice.None,
                    UpdateChoice.Earn(Vector.of(BigInteger.valueOf(20))),
                    UpdateChoice.ZKP(
                        UUID.randomUUID(),
                        "Some Update",
                        Vector.of(BigInteger.valueOf(30)),
                        Vector.of(BigInteger.valueOf(8)),
                        EmptyTokenUpdateMetadata()
                    )
                )
            )
        ),
        userTokenUpdateChoices = emptyList(),
        setUserUpdateChoice = { _, _ -> },
        {}
    )
}
