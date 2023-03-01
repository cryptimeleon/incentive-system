package org.cryptimeleon.incentive.app.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.ui.common.promotionImageUrl
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData
import java.math.BigInteger

@Composable
fun Dashboard(
    openSettings: () -> Unit,
    openBenchmark: () -> Unit,
    openAttacker: () -> Unit,
    navigateToPromotionDetails: (promotionId: BigInteger) -> Unit
) {
    val dashboardViewModel = hiltViewModel<DashboardViewModel>()
    val promotionDataList by dashboardViewModel.promotionDataListFlow.collectAsState(initial = emptyList())

    Dashboard(
        promotionDataList = promotionDataList,
        cardClicked = navigateToPromotionDetails,
        openSettings = openSettings,
        openBenchmark = openBenchmark,
        openAttacker = openAttacker
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    promotionDataList: List<PromotionData>,
    cardClicked: (BigInteger) -> Unit = {},
    openSettings: () -> Unit = {},
    openBenchmark: () -> Unit = {},
    openAttacker: () -> Unit = {}
) {
    Scaffold(topBar = {
        DefaultTopAppBar(
            onOpenSettings = openSettings,
            onOpenBenchmark = openBenchmark,
            onOpenAttacker = openAttacker
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
        PromotionImage(promotionImageUrl(promotionData = promotionData))
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
            .clip(RoundedCornerShape(8.dp)),
        error = painterResource(id = R.drawable.falllback)
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
    CryptimeleonPreviewContainer {
        TokenCard(
            PreviewData.hazelPromotionData, {}
        )
    }
}
