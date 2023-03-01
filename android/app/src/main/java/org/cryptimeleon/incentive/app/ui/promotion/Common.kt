package org.cryptimeleon.incentive.app.ui.promotion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors()
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
