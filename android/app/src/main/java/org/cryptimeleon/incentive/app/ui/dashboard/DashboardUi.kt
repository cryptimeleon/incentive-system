package org.cryptimeleon.incentive.app.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger

@Composable
fun Dashboard(
    openSettings: () -> Unit,
    openBenchmark: () -> Unit,
    navigateToPromotionDetails: (promotionId: BigInteger) -> Unit
) {
    val dashboardViewModel = hiltViewModel<DashboardViewModel>()
    val promotionDataList by dashboardViewModel.promotionDataListFlow.collectAsState(initial = emptyList())

    Dashboard(
        promotionDataList = promotionDataList,
        cardClicked = navigateToPromotionDetails,
        openSettings = openSettings,
        openBenchmark = openBenchmark
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    promotionDataList: List<PromotionData>,
    cardClicked: (BigInteger) -> Unit,
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
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            for (promotionData in promotionDataList) {
                TokenCard(
                    promotionData = promotionData,
                    cardClicked = { cardClicked(promotionData.pid) },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenCard(
    promotionData: PromotionData,
    cardClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .wrapContentHeight(),
        onClick = cardClicked,
    ) {
        PromotionImage(promotionData.promotionImageUrl)
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row {
                Text(
                    text = promotionData.promotionName,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .weight(1f)
                        .paddingFromBaseline(36.sp)
                )
                when (promotionData) {
                    is HazelPromotionData ->
                        Text(
                            text = "${promotionData.score}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.paddingFromBaseline(36.sp)
                        )
                    // TODO find nicer visualization
                    is VipPromotionData ->
                        Text(
                            text = "${promotionData.vipLevel}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.paddingFromBaseline(36.sp)
                        )
                    is StreakPromotionData ->
                        Text(
                            text = "${promotionData.streakCount}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.paddingFromBaseline(36.sp)
                        )
                }
            }
        }
    }
}

@Composable
private fun PromotionImage(imageUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Promotion Image",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(
    showBackground = true,
    name = "Dashboard Preview with Scaffold",
)
fun DashboardPreview() {
    CryptimeleonTheme {
        Scaffold {
            Box(Modifier.padding(it)) {
                Dashboard(
                    emptyList(),
                    {},
                    {},
                    {}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(
    showBackground = true,
    name = "Normal Promotion Item Preview",
)
fun PromotionItemPreview() {
    CryptimeleonTheme {
        Scaffold {
            Box(Modifier.padding(it)) {
                TokenCard(
                    HazelPromotionData(
                        "Promotion 1",
                        BigInteger.valueOf(17L),
                        "Promotion Description",
                        Vector.of(BigInteger.valueOf(34L)),
                        emptyList()
                    ),
                    {}
                )
            }
        }
    }
}
