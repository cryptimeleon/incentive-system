package org.cryptimeleon.incentive.app.ui.promotion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cryptimeleon.incentive.app.domain.usecase.RangeProofStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.SpendStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StreakDate
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun StreakPromotionTitle(streakPromotionData: StreakPromotionData) {
    val formatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMAN)
    val deadlineString =
        if (streakPromotionData.deadline is StreakDate.DATE) streakPromotionData.deadline.date.format(
            formatter
        ).toString() else "None"
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Streak: ${streakPromotionData.streakCount}")
        if (streakPromotionData.deadline is StreakDate.DATE && streakPromotionData.deadline.date.isBefore(
                LocalDate.now()
            )
        ) {
            Text(
                deadlineString,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(deadlineString, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun StreakBody(promotionData: StreakPromotionData) {
    PromotionInfoSectionHeader(text = "Rewards")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        promotionData.tokenUpdates.forEach { tokenUpdate ->
            when (tokenUpdate) {
                // todo distinguish proof and spend in UI
                is SpendStreakTokenUpdateState -> ZkpTokenUpdateCard(
                    tokenUpdate = tokenUpdate,
                    progressIfApplies = Optional.of(1f * tokenUpdate.currentStreak / tokenUpdate.requiredStreak)
                )
                is RangeProofStreakTokenUpdateState -> ZkpTokenUpdateCard(
                    tokenUpdate = tokenUpdate,
                    progressIfApplies = Optional.of(1f * tokenUpdate.currentStreak / tokenUpdate.requiredStreak)
                )
            }
        }
    }
}
