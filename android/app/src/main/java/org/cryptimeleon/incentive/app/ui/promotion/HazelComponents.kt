package org.cryptimeleon.incentive.app.ui.promotion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState
import java.util.*

@Composable
fun HazelPromotionTitle(hazelPromotionData: HazelPromotionData) =
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Points: ${hazelPromotionData.score}")
    }

@Composable
fun HazelBody(promotionData: HazelPromotionData) {
    PromotionInfoSectionHeader(text = "Rewards")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        promotionData.tokenUpdates.forEach { tokenUpdate ->
            when (tokenUpdate) {
                // TODO feasible design
                is HazelTokenUpdateState -> ZkpTokenUpdateCard(
                    tokenUpdate = tokenUpdate,
                    progressIfApplies = Optional.of(1f * tokenUpdate.current / tokenUpdate.goal)
                )
            }
        }
    }
}
