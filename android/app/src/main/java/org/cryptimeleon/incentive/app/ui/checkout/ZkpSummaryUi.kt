package org.cryptimeleon.incentive.app.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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

const val GEQ = "≥"
const val LEQ = "≤"

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
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = MaterialTheme.typography.bodySmall.toSpanStyle()
                                            .copy(fontFamily = FontFamily.Monospace)
                                    ) {
                                        append("You choose option \"${tokenUpdate.sideEffect.get()}\"\n")
                                        pushStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold))
                                        append("Your view:\n")
                                        pop()
                                        append("\tscore: ${tokenUpdate.current} ")
                                        arrow()
                                        append(" ${tokenUpdate.current - tokenUpdate.goal}\n")
                                        pushStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold))
                                        append("Store learns:\n")
                                        pop()
                                        append("\tscore_old $GEQ ${tokenUpdate.goal} AND score_new = score_old - ${tokenUpdate.goal}")
                                    }
                                },
                                inlineContent = arrowInlineContent
                            )
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
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = MaterialTheme.typography.bodySmall.toSpanStyle()
                                            .copy(fontFamily = FontFamily.Monospace)
                                    ) {
                                        append("You choose option \"Increase or Reset Streak\"\n")
                                        pushStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold))
                                        append("Your view:\n")
                                        pop()
                                        append("\tlastdate: ${if (tokenUpdate.lastDate is StreakDate.DATE) tokenUpdate.lastDate.date else "None"} ")
                                        arrow()
                                        append(" ${tokenUpdate.newLastDate}\n")
                                        append("\tstreak: ${tokenUpdate.currentStreak} ")
                                        arrow()
                                        append(" ${tokenUpdate.newCurrentStreak}\n")
                                        pushStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold))
                                        append("Store learns:\n")
                                        pop()
                                        append("\tnew_streak = 1 OR (\n\t\tnew_streak = old_streak + 1 \n\t\tAND ${tokenUpdate.newLastDate} - old_lastdate $LEQ ${tokenUpdate.intervalDays} days\n\t)\n")
                                        append("\tnew_lastdate = ${tokenUpdate.newLastDate}")
                                    }
                                },
                                inlineContent = arrowInlineContent
                            )
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

val arrowInlineContent = mapOf(
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

fun AnnotatedString.Builder.arrow() {
    appendInlineContent("arrow", "->")
}

@Preview
@Composable
fun ZkpSummaryUiPreview() {
    CryptimeleonPreviewContainer {
        ZkpSummaryUi(promotionDataList)
    }
}
