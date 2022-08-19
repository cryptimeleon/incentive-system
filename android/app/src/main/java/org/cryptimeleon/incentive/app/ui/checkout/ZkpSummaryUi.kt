package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.RangeProofStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.SpendStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StandardStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData.Companion.hazelPromotionData
import org.cryptimeleon.incentive.app.ui.preview.PreviewData.Companion.streakPromotionData
import org.cryptimeleon.incentive.app.ui.preview.PreviewData.Companion.vipPromotionData

const val GEQ = "≥"
const val LEQ = "≤"

@Composable
fun ZkpSummaryUi(promotionData: PromotionData) {
    promotionData.tokenUpdates.filter { t -> t.isSelected() && t.isFeasible() }
        .forEach { tokenUpdate ->
            when (promotionData) {
                is HazelPromotionData -> Column {
                    when (tokenUpdate) {
                        is HazelTokenUpdateState -> {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = MaterialTheme.typography.bodySmall.toSpanStyle()
                                            .copy(fontFamily = FontFamily.Monospace)
                                    ) {
                                        append("You choose option \"${tokenUpdate.sideEffect.get()}\"\n")
                                        yourView()
                                        append("\tscore: ${tokenUpdate.current} ")
                                        arrow()
                                        if (tokenUpdate.basketPoints > 0) append(" ${tokenUpdate.basketPoints} - ${tokenUpdate.goal} =")
                                        append(" ${tokenUpdate.current + tokenUpdate.basketPoints - tokenUpdate.goal}\n")
                                        storeLearns()
                                        append("\tAND\n")
                                        append("\t├── score_old + ${tokenUpdate.basketPoints} $GEQ ${tokenUpdate.goal}\n")
                                        append("\t└── score_new = score_old + ${tokenUpdate.basketPoints} - ${tokenUpdate.goal}")
                                    }
                                },
                                inlineContent = arrowInlineContent
                            )
                        }
                        is EarnTokenUpdate -> {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = MaterialTheme.typography.bodySmall.toSpanStyle()
                                            .copy(fontFamily = FontFamily.Monospace)
                                    ) {
                                        append("You choose option \"Earn\"\n")
                                        yourView()
                                        append("\tscore: ${tokenUpdate.currentPoints.get(0)} ")
                                        arrow()
                                        append(" ${tokenUpdate.targetPoints.get(0)}\n")
                                        storeLearns()
                                        append(
                                            "\tscore_new = score_old + ${
                                                tokenUpdate.addedPoints.get(0)
                                            } "
                                        )
                                    }
                                },
                                inlineContent = arrowInlineContent
                            )
                        }
                        is NoTokenUpdate -> NothingText()
                    }
                }
                is StreakPromotionData -> Column {
                    when (tokenUpdate) {
                        is StandardStreakTokenUpdateState -> {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = MaterialTheme.typography.bodySmall.toSpanStyle()
                                            .copy(fontFamily = FontFamily.Monospace)
                                    ) {
                                        append("You choose option \"Increase or Reset Streak\"\n")
                                        yourView()
                                        append("\tlastdate: ${tokenUpdate.lastDate} ")
                                        arrow()
                                        append(" ${tokenUpdate.newLastDate}\n")
                                        append("\tstreak: ${tokenUpdate.currentStreak} ")
                                        arrow()
                                        append(" ${tokenUpdate.newCurrentStreak}\n")
                                        storeLearns()
                                        append("\tnew_streak = 1 OR (\n\t\tnew_streak = old_streak + 1 \n\t\tAND ${tokenUpdate.newLastDate} - old_lastdate $LEQ ${tokenUpdate.intervalDays} days\n\t)\n")
                                        append("\tnew_lastdate = ${tokenUpdate.newLastDate}")
                                    }
                                },
                                inlineContent = arrowInlineContent
                            )
                        }
                        is RangeProofStreakTokenUpdateState -> {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = MaterialTheme.typography.bodySmall.toSpanStyle()
                                            .copy(fontFamily = FontFamily.Monospace)
                                    ) {
                                        append("You choose option \"Update Streak and Proof Streak Length\"\n")
                                        yourView()
                                        append("\tlastdate: ${tokenUpdate.lastDate} ")
                                        arrow()
                                        append(" ${tokenUpdate.newLastDate}\n")
                                        append("\tstreak: ${tokenUpdate.currentStreak} ")
                                        arrow()
                                        append(" ${tokenUpdate.newCurrentStreak}\n")
                                        storeLearns()
                                        append("\tnew_streak = 1 OR (\n\t\tnew_streak = old_streak + 1 \n\t\tAND ${tokenUpdate.newLastDate} - old_lastdate $LEQ ${tokenUpdate.intervalDays} days\n\t)\n")
                                        append("\tnew_lastdate = ${tokenUpdate.newLastDate}")
                                    }
                                },
                                inlineContent = arrowInlineContent
                            )
                        }
                        is SpendStreakTokenUpdateState -> {}
                        is EarnTokenUpdate -> {}
                        is NoTokenUpdate -> NothingText()
                    }
                }
            }
        }
}

private val arrowInlineContent = mapOf(
    Pair(
        "arrow",
        InlineTextContent(
            Placeholder(
                width = 1.0.em,
                height = 1.0.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Icon(Icons.Default.TrendingFlat, contentDescription = "Arrow")
        }
    )
)

private fun AnnotatedString.Builder.yourView() {
    pushStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold))
    append("Your view:\n")
    pop()
}

private fun AnnotatedString.Builder.storeLearns() {
    pushStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold))
    append("Store learns:\n")
    pop()
}

private fun AnnotatedString.Builder.arrow() {
    appendInlineContent("arrow", "->")
}

@Composable
private fun NothingText() {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.bodySmall.toSpanStyle()
                    .copy(fontFamily = FontFamily.Monospace)
            ) {
                append("You choose option \"Nothing\"")
            }
        },
        inlineContent = arrowInlineContent
    )
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
