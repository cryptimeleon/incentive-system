package org.cryptimeleon.incentive.app.ui.log

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.ProveVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.RangeProofStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.SpendStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StandardStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.UpgradeVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
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
                    is SpendStreakTokenUpdateState -> {}
                    is NoTokenUpdate -> NothingText()
                }
                is VipPromotionData -> when (tokenUpdate) {
                    is UpgradeVipTokenUpdateState -> {}
                    is ProveVipTokenUpdateState -> {}
                    is EarnTokenUpdate -> {}
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
