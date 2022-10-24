package org.cryptimeleon.incentive.app.ui.promotion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cryptimeleon.incentive.app.domain.usecase.ZkpTokenUpdate
import java.util.*

@Composable
fun PromotionInfoSectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = HzPadding.padding(vertical = 8.dp),
    )
}

@Composable
fun ZkpTokenUpdateCard(
    tokenUpdate: ZkpTokenUpdate,
    progressIfApplies: Optional<Float>,
    colors: CardColors = CardDefaults.cardColors(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = colors
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (tokenUpdate.sideEffect.isPresent) {
                    Text(
                        tokenUpdate.sideEffect.get(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Text(tokenUpdate.description, style = MaterialTheme.typography.bodyMedium)
            }
            if (progressIfApplies.isPresent) {
                CircularProgressIndicator(progress = progressIfApplies.get())
            }
        }
    }
}
