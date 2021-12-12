package org.cryptimeleon.incentive.app.ui.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar

@Composable
fun Dashboard(openSettings: () -> Unit, openBenchmark: () -> Unit) {
    val dashboardViewModel = hiltViewModel<DashboardViewModel>()
    val state by dashboardViewModel.state.collectAsState(DashboardState(emptyList()))
    Dashboard(dashboardState = state, openSettings = openSettings, openBenchmark = openBenchmark)
}

@Composable
fun Dashboard(
    dashboardState: DashboardState,
    openSettings: () -> Unit = {},
    openBenchmark: () -> Unit = {}
) {
    Scaffold(topBar = {
        DefaultTopAppBar(
            onOpenSettings = openSettings,
            onOpenBenchmark = openBenchmark
        )
    }) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            for (promotionState in dashboardState.promotionStates) {
                TokenCard(promotionState = promotionState)
            }
        }
    }
}

@Composable
fun TokenCard(promotionState: PromotionState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f) // Unweighted components are measured first
            ) {
                Text(
                    text = promotionState.title,
                    style = MaterialTheme.typography.h5,
                )
                Text(
                    text = promotionState.description,
                    style = MaterialTheme.typography.body1,
                )
                Text(
                    text = "${promotionState.count} Points",
                    style = MaterialTheme.typography.body2,
                )
            }
            Icon(
                Icons.Outlined.Redeem,
                contentDescription = "Promotion Icon",
                modifier = Modifier
                    .requiredSize(64.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun DashboardPreview() {
    CryptimeleonTheme {
        val dashboardState = remember {
            DashboardState(
                listOf(
                    PromotionState(count = 7),
                    PromotionState(
                        title = "Other Promotion",
                        description = "You can win a pan if you're really really really lucky",
                        20
                    )
                )
            )
        }
        Dashboard(dashboardState)
    }
}

@Composable
@Preview(
    showBackground = true,
    name = "Dark Mode"
)
fun DashboardPreviewLight() {
    DashboardPreview()
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
fun DashboardPreviewDark() {
    DashboardPreview()
}
