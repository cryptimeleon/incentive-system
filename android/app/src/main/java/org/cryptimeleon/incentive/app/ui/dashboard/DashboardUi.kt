package org.cryptimeleon.incentive.app.ui.dashboard

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
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
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            for (promotionState in dashboardState.promotionStates) {
                TokenCard(
                    promotionState = promotionState,
                    expandedPromotion = expandedPromotion,
                    toggleExpanded = { id ->
                        expandedPromotion = if (expandedPromotion == id) "" else id
                    }
                )
            }
            Spacer(modifier = Modifier.height(0.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenCard(
    promotionState: PromotionState,
    expandedPromotion: String,
    toggleExpanded: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .wrapContentHeight(),
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
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .weight(1f)
                        .paddingFromBaseline(36.sp)
                )
                when (promotionState) {
                    is HazelPromotionState ->
                        Text(
                            text = "${promotionState.count}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.paddingFromBaseline(36.sp)
                        )
                    // TODO find nicer visualization
                    is VipPromotionState ->
                        Text(
                            text = "${promotionState.status}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.paddingFromBaseline(36.sp)
                        )
                    is StreakPromotionState ->
                        Text(
                            text = "${promotionState.streak}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.paddingFromBaseline(36.sp)
                        )
                }
            }
            Text(
                text = promotionState.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (expandedPromotion == promotionState.id) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Redeem,
                        contentDescription = "Rewards Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .requiredSize(32.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Rewards",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Display points matching the promotion
                val tokenUpdateStateIterator: Iterator<TokenUpdateState> =
                    promotionState.updates.iterator()
                while (tokenUpdateStateIterator.hasNext()) {
                    when (val rewardState = tokenUpdateStateIterator.next()) {
                        is HazelTokenUpdateState -> {
                            Text(
                                text = rewardState.sideEffect,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            LinearProgressIndicator(
                                progress = if (rewardState.current > rewardState.goal) 1f else rewardState.current / (rewardState.goal * 1.0f),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${rewardState.goal}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        is VipTokenUpdateState -> {
                            Text(
                                text = "Advantage for ${rewardState.requiredStatus} VIP",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = rewardState.sideEffect,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.End),
                            )
                        }
                        is UpgradeVipTokenUpdateState -> {
                            Text(
                                text = rewardState.sideEffect,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            LinearProgressIndicator(
                                progress = if (rewardState.currentPoints > rewardState.requiredPoints) 1f else rewardState.currentPoints / (rewardState.requiredPoints * 1.0f),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${rewardState.requiredPoints}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        is SpendStreakTokenUpdateState -> {
                            Text(
                                text = rewardState.sideEffect,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            // TODO handle 'out of streak', add this to state!
                            LinearProgressIndicator(
                                progress = if (rewardState.currentStreak > rewardState.requiredStreak) 1f else rewardState.currentStreak / (rewardState.requiredStreak * 1.0f),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${rewardState.requiredStreak}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        is RangeProofStreakTokenUpdateState -> {
                            Text(
                                text = rewardState.sideEffect,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            LinearProgressIndicator(
                                progress = if (rewardState.currentStreak > rewardState.requiredStreak) 1f else rewardState.currentStreak / (rewardState.requiredStreak * 1.0f),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${rewardState.requiredStreak}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        is StandardStreakTokenUpdateState -> {
                            // TODO do we want to visualize this?
                        }
                    }
                    if (tokenUpdateStateIterator.hasNext()) {
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
        updates = listOf(
            HazelTokenUpdateState("Description", "Hazelnut Spread", 3, 5),
            HazelTokenUpdateState("Description", "Large Hazelnut Spread", 3, 8),
        ),
    )
val secondPromotionState =
    HazelPromotionState(
        id = "2",
        title = "Other Promotion",
        description = "You can win a pan if you're really really really lucky",
        count = 3,
        updates = listOf(
            HazelTokenUpdateState("Description", "Pan", 3, 5)
        ),
    )
val thirdPromotionState =
    VipPromotionState(
        id = "3",
        title = "VIP Promotion",
        description = "You can reach VIP levels bronze, silver and gold for spending money.",
        updates = listOf(
            VipTokenUpdateState("Description", "2% Discount", VipStatus.BRONZE, VipStatus.BRONZE),
            UpgradeVipTokenUpdateState(
                "Description",
                "Become Silver VIP",
                2734,
                3000,
                VipStatus.SILVER
            ),
            UpgradeVipTokenUpdateState(
                "Description",
                "Become Gold VIP",
                2734,
                10000,
                VipStatus.GOLD
            )
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
