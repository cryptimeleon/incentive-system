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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Redeem,
                    contentDescription = "Promotion Icon",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .requiredSize(32.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = promotionState.title,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                style = MaterialTheme.typography.body2,
                text = buildAnnotatedString {
                    append("Points: ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
                        append(
                            promotionState.count.joinToString(
                                prefix = "(",
                                separator = ", ",
                                postfix = ")",
                            ) { point -> "$point" },
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = promotionState.description,
                style = MaterialTheme.typography.body1,
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
                    PromotionState(
                        title = "First Promotion",
                        description = "Get free nutella for buying nutella",
                        rewards = listOf("Nutella", "Big Nutella"),
                        listOf(3)
                    ),
                    PromotionState(
                        title = "Other Promotion",
                        description = "You can win a pan if you're really really really lucky",
                        rewards = listOf("Pan", "Wok"),
                        listOf(2, 0, 3)
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
