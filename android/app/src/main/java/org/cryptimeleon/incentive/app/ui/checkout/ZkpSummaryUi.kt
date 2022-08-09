package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.StandardStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StreakDate
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData.Companion.promotionDataList

@Composable
fun ZkpSummaryUi(promotionDataList: List<PromotionData>) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        promotionDataList.forEach { promotionData ->
            val tokenUpdate =
                promotionData.tokenUpdates.first { t -> t.isSelected() && t.isFeasible() }
            when (promotionData) {
                is HazelPromotionData -> Column {
                    when (tokenUpdate) {
                        is HazelTokenUpdateState -> {
                            Text("ZKP Token Update")
                            Text("Before: score = ${tokenUpdate.current}")
                            Text("Store learns: score >= ${tokenUpdate.goal}")
                            Text("After: score = ${tokenUpdate.current - tokenUpdate.goal}")
                        }
                        is EarnTokenUpdate -> {
                            Text("Earn Token Update")
                            Text("Before: score = ${tokenUpdate.currentPoints}")
                            Text("Store learns: score += ${tokenUpdate.addedPoints}")
                            Text("After: score = ${tokenUpdate.targetPoints}")
                        }
                        is NoTokenUpdate -> Text(tokenUpdate.description)
                    }
                }
                is StreakPromotionData -> Column {
                    when (tokenUpdate) {
                        is StandardStreakTokenUpdateState -> {
                            Text("ZKP Token Update")
                            Text("Before: lastdate = ${if (tokenUpdate.lastDate is StreakDate.DATE) tokenUpdate.lastDate.date else "None"}, streak = ${tokenUpdate.currentStreak}")
                            Text("Store learns: lastdate = ${tokenUpdate.newLastDate} AND [streak += 1 OR streak = 1]")
                            Text("After: lastdate = ${tokenUpdate.newLastDate}, streak = ${tokenUpdate.newCurrentStreak}")
                        }
                        is EarnTokenUpdate -> {
                        }
                        is NoTokenUpdate -> Text(tokenUpdate.description)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ZkpSummaryUiPreview() {
    CryptimeleonPreviewContainer {
        ZkpSummaryUi(promotionDataList)
    }
}
