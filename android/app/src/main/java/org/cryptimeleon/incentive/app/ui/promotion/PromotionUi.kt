package org.cryptimeleon.incentive.app.ui.promotion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionUpdateFeasibility
import org.cryptimeleon.incentive.app.domain.usecase.ProveVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.RangeProofStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.SpendStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StandardStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.UpgradeVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipStatus
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger
import java.util.*

@Composable
fun PromotionDetailUi(promotionId: BigInteger, onUpClicked: () -> Unit) {
    val promotionViewModel = hiltViewModel<PromotionViewModel>()
    val promotionData: PromotionData? by promotionViewModel.promotionDataFlowFor(promotionId)
        .collectAsState(initial = null)

    // TODO up press
    promotionData?.let {
        PromotionDetailUi(it, onUpClicked)
    }

    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromotionDetailUi(promotionData: PromotionData, back: () -> Unit) {
    Box(
        modifier = Modifier
            //.windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxSize()
    ) {
        val scroll = rememberScrollState()
        Column {
            Header(promotionData.promotionImageUrl)
            Column(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(16.dp)
            ) {
                Text(
                    promotionData.promotionName,
                    style = MaterialTheme.typography.headlineLarge
                )
                promotionData.tokenUpdates.forEach {
                    Text(it.description)
                }
            }
        }
        FilledTonalIconButton(
            onClick = back,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}

@Composable
private fun Header(imageUrl: String) {
    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Promotion Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                    )
                )
        )
    }
}

val promotionDataList = listOf<PromotionData>(
    HazelPromotionData(
        promotionName = "Nutella Promotion",
        pid = BigInteger.valueOf(5345L),
        promotionDescription = "Earn points for buying Nutella!",
        points = Vector.of(BigInteger.valueOf(6L)),
        tokenUpdates = listOf(
            NoTokenUpdate(),
            EarnTokenUpdate(PromotionUpdateFeasibility.CANDIDATE, "Earn 2 Points"),
            HazelTokenUpdateState(
                description = "Get a free glass of Nutella",
                sideEffect = Optional.of("Free Nutella"),
                feasibility = PromotionUpdateFeasibility.SELECTED,
                current = 6,
                goal = 4
            )
        )
    ),
    VipPromotionData(
        promotionName = "VIP Promotion",
        pid = BigInteger.valueOf(3453L),
        promotionDescription = "Become BRONZE, SIlVER or GOLD by collecting points!",
        tokenUpdates = listOf(
            NoTokenUpdate(feasibility = PromotionUpdateFeasibility.SELECTED),
            ProveVipTokenUpdateState(
                description = "Prove you are SILVER",
                sideEffect = Optional.of("5% Discount"),
                feasibility = PromotionUpdateFeasibility.CANDIDATE,
                currentStatus = VipStatus.SILVER,
                requiredStatus = VipStatus.SILVER
            ),
            ProveVipTokenUpdateState(
                description = "Prove you are GOLD",
                sideEffect = Optional.of("10% Discount"),
                feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                currentStatus = VipStatus.SILVER,
                requiredStatus = VipStatus.GOLD
            ),
            UpgradeVipTokenUpdateState(
                description = "Become GOLD",
                sideEffect = Optional.of("10% Discount"),
                feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                currentPoints = 250,
                requiredPoints = 300,
                targetVipStatus = VipStatus.GOLD
            )
        ),
        points = Vector.of(BigInteger.valueOf(234), BigInteger.valueOf(2)),
    ),
    StreakPromotionData(
        promotionName = "Streak Promotion",
        pid = BigInteger.valueOf(3467L),
        promotionDescription = "Increase your streak by shopping within a week",
        tokenUpdates = listOf(
            NoTokenUpdate(),
            StandardStreakTokenUpdateState(
                description = "Update your streak",
                sideEffect = Optional.empty(),
                feasibility = PromotionUpdateFeasibility.SELECTED
            ),
            RangeProofStreakTokenUpdateState(
                description = "Prove that streak is at least 5",
                sideEffect = Optional.of("Free Coffee"),
                feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                requiredStreak = 10,
                currentStreak = 3
            ),
            SpendStreakTokenUpdateState(
                description = "Spend streak to get a reward",
                sideEffect = Optional.of("Teddy Bear"),
                feasibility = PromotionUpdateFeasibility.CANDIDATE,
                requiredStreak = 3,
                currentStreak = 3
            )
        ),
        streakInterval = 7,
        points = Vector.of(BigInteger.valueOf(3L), BigInteger.valueOf(34654L))
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Preview() {
    CryptimeleonTheme {
        Scaffold {
            Box(Modifier.padding(it)) {
                PromotionDetailUi(promotionDataList.get(0), {})
            }
        }
    }
}

/*
            Text(
                text = promotionData.promotionDescription,
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

 */
