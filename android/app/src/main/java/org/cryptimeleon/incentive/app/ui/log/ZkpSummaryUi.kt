package org.cryptimeleon.incentive.app.ui.log

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cryptimeleon.incentive.app.domain.usecase.*
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData.Companion.hazelPromotionData
import org.cryptimeleon.incentive.app.ui.preview.PreviewData.Companion.streakPromotionData
import org.cryptimeleon.incentive.app.ui.preview.PreviewData.Companion.vipPromotionData

@Composable
fun ZkpSummaryUi(promotionData: PromotionData) {
    promotionData.tokenUpdates.filter { t -> t.isSelected() && t.isFeasible() }
        .forEach { tokenUpdate ->
            when (promotionData) {
                is HazelPromotionData -> when (tokenUpdate) {
                    is HazelTokenUpdateState -> HazelTokenUpdateLog(tokenUpdate)
                    is EarnTokenUpdate -> HazelEarnLog(tokenUpdate)
                    is NoTokenUpdate -> NothingText()
                }
                is StreakPromotionData -> when (tokenUpdate) {
                    is StandardStreakTokenUpdateState -> StandardStreakLog(tokenUpdate)
                    is RangeProofStreakTokenUpdateState -> RangeProofLog(tokenUpdate)
                    is NoTokenUpdate -> NothingText()
                }
                is VipPromotionData -> when (tokenUpdate) {
                    is UpgradeVipTokenUpdateState -> UpgradeVipLog(tokenUpdate)
                    is ProveVipTokenUpdateState -> ProveVipLog(tokenUpdate)
                    is EarnTokenUpdate -> VipEarnLog(tokenUpdate)
                    is NoTokenUpdate -> NothingText()
                }
            }
        }
}


@Preview
@Composable
private fun HazelPreview() {
    CryptimeleonPreviewContainer {
        ZkpSummaryUi(hazelPromotionData)
    }
}

@Preview
@Composable
private fun VipPreview() {
    CryptimeleonPreviewContainer {
        ZkpSummaryUi(vipPromotionData)
    }
}

@Preview
@Composable
private fun StreakPreview() {
    CryptimeleonPreviewContainer {
        ZkpSummaryUi(streakPromotionData)
    }
}
