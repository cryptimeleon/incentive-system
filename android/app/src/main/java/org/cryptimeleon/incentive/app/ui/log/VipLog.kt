package org.cryptimeleon.incentive.app.ui.log

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ProveVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.UpgradeVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.VipStatus


@Composable
fun VipEarnLog(tokenUpdate: EarnTokenUpdate) {
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
                        tokenUpdate.addedPoints.get(0).toInt()
                    }\n"
                )
                yourView()
                append("\tvip_level = ${VipStatus.fromInt(tokenUpdate.currentPoints.get(1))}\n")
                append("\tscore_old = ${tokenUpdate.currentPoints.get(0)} ")
                arrow()
                append(" score_new = ${tokenUpdate.targetPoints.get(0)}")
            }
        },
        inlineContent = arrowInlineContent
    )
}

@Composable
fun UpgradeVipLog(tokenUpdate: UpgradeVipTokenUpdateState) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.bodySmall.toSpanStyle()
                    .copy(fontFamily = FontFamily.Monospace)
            ) {
                storesView()
                append("Choice: \"${tokenUpdate.sideEffect.get()}\"\n")
                append("Update Tree:\n")
                append("\tAND\n")
                append("\t├── vip_level_old = ${tokenUpdate.currentVipStatus}\n")
                append("\t├── vip_level_new = ${tokenUpdate.targetVipStatus}\n")
                append("\t├── score_new = score_old + ${tokenUpdate.basketPoints}\n")
                append("\t└── score_new $GEQ ${tokenUpdate.requiredPoints}\n")
                yourView()
                append("\tscore_old = ${tokenUpdate.currentPoints} ")
                arrow()
                append(" score_new = ${tokenUpdate.currentPoints + tokenUpdate.basketPoints}\n")
            }
        },
        inlineContent = arrowInlineContent
    )
}

@Composable
fun ProveVipLog(tokenUpdate: ProveVipTokenUpdateState) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.bodySmall.toSpanStyle()
                    .copy(fontFamily = FontFamily.Monospace)
            ) {
                storesView()
                append("Choice: \"${tokenUpdate.sideEffect.get()}\"\n")
                append("Update Tree:\n")
                append("\tAND\n")
                append("\t├── vip_level = ${tokenUpdate.requiredStatus}\n")
                append("\t├── score_new = score_old + ${tokenUpdate.basketPoints}\n")
                yourView()
                append("\tscore_old = ${tokenUpdate.currentPoints} ")
                arrow()
                append(" score_new = ${tokenUpdate.currentPoints + tokenUpdate.basketPoints}\n")
            }
        },
        inlineContent = arrowInlineContent
    )
}
