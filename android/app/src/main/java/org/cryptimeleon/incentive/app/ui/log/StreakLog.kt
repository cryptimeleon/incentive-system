package org.cryptimeleon.incentive.app.ui.log

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import org.cryptimeleon.incentive.app.domain.usecase.RangeProofStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StandardStreakTokenUpdateState

@Composable
fun StandardStreakLog(tokenUpdate: StandardStreakTokenUpdateState) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.bodySmall.toSpanStyle()
                    .copy(fontFamily = FontFamily.Monospace)
            ) {
                storesView()
                append("Choice: \"Increase or Reset Streak\"\n")
                append("Update Tree:\n")
                append("\tAND\n")
                append("\t├─ new_lastdate = ${tokenUpdate.newLastDate}\n")
                append("\t└─ OR\n")
                append("\t   ├─ new_streak = 1\n")
                append("\t   └─ AND\n")
                append("\t      ├─ new_streak = old_streak + 1 \n")
                append("\t      └─ ${tokenUpdate.newLastDate} - old_lastdate $LEQ ${tokenUpdate.intervalDays} days\n")
                yourView()
                append("\tlastdate: ${tokenUpdate.lastDate.toString()} ")
                arrow()
                append(" ${tokenUpdate.newLastDate}\n")
                append("\tstreak: ${tokenUpdate.currentStreak} ")
                arrow()
                append(" ${tokenUpdate.newCurrentStreak}")
            }
        },
        inlineContent = arrowInlineContent
    )
}

@Composable
fun RangeProofLog(tokenUpdate: RangeProofStreakTokenUpdateState) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.bodySmall.toSpanStyle()
                    .copy(fontFamily = FontFamily.Monospace)
            ) {
                storesView()
                append("Choice: \"Update Streak and Proof Streak Length\"\n")
                append("Update Tree:\n")
                append("\tAND\n")
                append("\t├─ new_lastdate = ${tokenUpdate.newLastDate}\n")
                append("\t├─ new_streak = old_streak + 1 \n")
                append("\t├─ new_streak $GEQ ${tokenUpdate.requiredStreak}\n")
                append("\t└─ ${tokenUpdate.newLastDate} - old_lastdate $LEQ ${tokenUpdate.intervalDays} days\n")
                yourView()
                append("\tlastdate: ${tokenUpdate.lastDate} ")
                arrow()
                append(" ${tokenUpdate.newLastDate}\n")
                append("\tstreak: ${tokenUpdate.currentStreak} ")
                arrow()
                append(" ${tokenUpdate.newCurrentStreak}")
            }
        },
        inlineContent = arrowInlineContent
    )
}
