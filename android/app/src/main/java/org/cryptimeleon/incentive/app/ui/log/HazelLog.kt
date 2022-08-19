package org.cryptimeleon.incentive.app.ui.log

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState

@Composable
fun HazelEarnLog(tokenUpdate: EarnTokenUpdate) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.bodySmall.toSpanStyle()
                    .copy(fontFamily = FontFamily.Monospace)
            ) {
                storesView()
                append("Choice: \"Earn\"\n")
                append(
                    "\tscore_new = score_old + ${
                        tokenUpdate.addedPoints.get(
                            0
                        )
                    }\n"
                )
                yourView()
                append("\tscore: ${tokenUpdate.currentPoints.get(0)} ")
                arrow()
                append(" ${tokenUpdate.targetPoints.get(0)}")
            }
        },
        inlineContent = arrowInlineContent
    )
}

@Composable
fun HazelTokenUpdateLog(tokenUpdate: HazelTokenUpdateState) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.bodySmall.toSpanStyle()
                    .copy(fontFamily = FontFamily.Monospace)
            ) {
                storesView()
                append("Choice: \"${tokenUpdate.sideEffect.get()}\"\n")
                append("${tokenUpdate.basketPoints} Nutella in basket. ")
                append(
                    "${tokenUpdate.basketPoints} point${pluralS(tokenUpdate.basketPoints)} will be added. "
                )
                append("${tokenUpdate.goal} point${pluralS(tokenUpdate.goal)} will be deducted.\n")
                append("Update Tree:\n")
                append("\tAND\n")
                append("\t├── score_old + ${tokenUpdate.basketPoints} $GEQ ${tokenUpdate.goal}\n")
                append("\t└── score_new = score_old + ${tokenUpdate.basketPoints} - ${tokenUpdate.goal}\n")
                yourView()
                append("\tscore_old ${tokenUpdate.current} ")
                arrow()
                append(" score_new")
                if (tokenUpdate.basketPoints > 0) append(" ${tokenUpdate.basketPoints} - ${tokenUpdate.goal} =")
                append(" ${tokenUpdate.current + tokenUpdate.basketPoints - tokenUpdate.goal}\n")
            }
        },
        inlineContent = arrowInlineContent
    )
}
