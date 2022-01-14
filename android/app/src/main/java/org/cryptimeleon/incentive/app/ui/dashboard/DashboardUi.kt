package org.cryptimeleon.incentive.app.ui.dashboard

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar

@Composable
fun Dashboard(openSettings: () -> Unit, openBenchmark: () -> Unit) {
    val dashboardViewModel = hiltViewModel<DashboardViewModel>()
    val state by dashboardViewModel.state.collectAsState(DashboardState(emptyList()))
    Dashboard(dashboardState = state, openSettings = openSettings, openBenchmark = openBenchmark)
}

@Composable
fun Dashboard(
    dashboardState: DashboardState,
    openSettings: () -> Unit = {},
    openBenchmark: () -> Unit = {}
) {
    var expandedPromotion by remember { mutableStateOf("") }
    Scaffold(topBar = {
        DefaultTopAppBar(
            onOpenSettings = openSettings,
            onOpenBenchmark = openBenchmark
        )
    }) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            for (promotionState in dashboardState.promotionStates) {
                TokenCard(
                    promotionState = promotionState,
                    expandedPromotion = expandedPromotion,
                    toggleExpanded = { id ->
                        expandedPromotion = if (expandedPromotion == id) "" else id
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TokenCard(
    promotionState: PromotionState,
    expandedPromotion: String,
    toggleExpanded: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = 4.dp,
        onClick = { toggleExpanded(promotionState.id) },
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row {
                Text(
                    text = promotionState.title,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier
                        .weight(1f)
                        .paddingFromBaseline(36.sp)
                )
                when (promotionState) {
                    is HazelPromotionState -> {
                        Text(
                            text = "${promotionState.count}",
                            style = MaterialTheme.typography.h4,
                            color = MaterialTheme.colors.secondary,
                            modifier = Modifier.paddingFromBaseline(36.sp)
                        )
                    }
                    // TODO find nicer visualization
                    is VipPromotionState -> {
                        Text(
                            text = "${promotionState.status}",
                            style = MaterialTheme.typography.h4,
                            color = MaterialTheme.colors.secondary,
                            modifier = Modifier.paddingFromBaseline(36.sp)
                        )
                    }
                }
            }
            Text(
                text = promotionState.description,
                style = MaterialTheme.typography.body1,
            )
            if (true || expandedPromotion == promotionState.id) { // TODO decide on one option
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Redeem,
                        contentDescription = "Rewards Icon",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .requiredSize(32.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Rewards",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Display points matching the promotion
                val rewardStateIterator: Iterator<RewardState> = promotionState.rewards.iterator()
                while (rewardStateIterator.hasNext()) {
                    when (val rewardState = rewardStateIterator.next()) {
                        is HazelRewardState -> {
                            Text(
                                text = rewardState.sideEffect,
                                style = MaterialTheme.typography.body1,
                            )
                            LinearProgressIndicator(
                                progress = if (rewardState.current > rewardState.goal) 1f else rewardState.current / (rewardState.goal * 1.0f),
                                color = MaterialTheme.colors.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${rewardState.goal} ",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        is VipRewardState -> {
                            Text(
                                text = "Advantage for ${rewardState.requiredStatus} VIP",
                                style = MaterialTheme.typography.body1,
                            )
                            Text(
                                text = rewardState.sideEffect,
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.End),
                            )
                        }
                        is UpgradeVipRewardState -> {
                            Text(
                                text = rewardState.sideEffect,
                                style = MaterialTheme.typography.body1,
                            )
                            LinearProgressIndicator(
                                progress = if (rewardState.currentPoints > rewardState.requiredPoints) 1f else rewardState.currentPoints / (rewardState.requiredPoints * 1.0f),
                                color = MaterialTheme.colors.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${rewardState.requiredPoints} ",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                    if (rewardStateIterator.hasNext()) {
                        Divider(Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

const val uiMode = Configuration.UI_MODE_NIGHT_NO
val firstPromotionState =
    HazelPromotionState(
        id = "1",
        title = "First Promotion",
        description = "Get free Hazelnut Spread for buying Hazelnut Spread",
        count = 3,
        rewards = listOf(
            HazelRewardState("Description", "Hazelnut Spread", 3, 5),
            HazelRewardState("Description", "Large Hazelnut Spread", 3, 8),
        ),
    )
val secondPromotionState =
    HazelPromotionState(
        id = "2",
        title = "Other Promotion",
        description = "You can win a pan if you're really really really lucky",
        count = 3,
        rewards = listOf(
            HazelRewardState("Description", "Pan", 3, 5)
        ),
    )
val thirdPromotionState =
    VipPromotionState(
        id = "3",
        title = "VIP Promotion",
        description = "You can reach VIP levels bronze, silver and gold for spending money.",
        rewards = listOf(
            VipRewardState("Description", "2% Discount", VipStatus.BRONZE, VipStatus.BRONZE),
            UpgradeVipRewardState("Description", "Become Silver VIP", 2734, 3000, VipStatus.SILVER),
            UpgradeVipRewardState("Description", "Become Gold VIP", 2734, 10000, VipStatus.GOLD)
        ),
        2734,
        VipStatus.BRONZE
    )

@Composable
@Preview(
    showBackground = true,
    name = "Dashboard Preview with Scaffold",
    uiMode = uiMode,
)
fun DashboardPreview() {
    CryptimeleonTheme {
        val dashboardState = remember {
            DashboardState(
                listOf(
                    firstPromotionState,
                    thirdPromotionState,
                    secondPromotionState,
                )
            )
        }
        Dashboard(dashboardState)
    }
}

@Composable
@Preview(
    showBackground = true,
    name = "Normal Promotion Item Preview",
    uiMode = uiMode,
)
fun PromotionItemPreview() {
    TokenCard(
        promotionState = firstPromotionState,
        expandedPromotion = "",
        toggleExpanded = {})
}

@Composable
@Preview(
    showBackground = true,
    name = "Expanded Promotion Item Preview",
    uiMode = uiMode,
)
fun ExpandedPromotionItemPreview() {
    TokenCard(promotionState = firstPromotionState,
        expandedPromotion = firstPromotionState.id,
        toggleExpanded = {})
}
