package org.cryptimeleon.incentive.app.ui.promotion

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.domain.usecase.*
import org.cryptimeleon.incentive.app.ui.common.promotionImageUrl
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData
import java.math.BigInteger

val IMAGE_HEIGHT = 280.dp
val IMAGE_SCROLL = 140.dp

val MIN_HEADER_OFFSET = 56.dp
val MAX_HEADER_OFFSET = MIN_HEADER_OFFSET + IMAGE_SCROLL
val MIN_HEADER_SIZE = 100.dp

val CREDIT_CARD_HEIGHT = 60.dp

val HzPadding = Modifier.padding(horizontal = 16.dp)


@Composable
fun PromotionDetailUi(promotionId: BigInteger, onUpClicked: () -> Unit) {
    val promotionViewModel = hiltViewModel<PromotionViewModel>()
    val promotionData: PromotionData? by promotionViewModel.promotionDataFlowFor(promotionId)
        .collectAsState(initial = null)

    promotionData?.let {
        PromotionDetailUi(it, onUpClicked)
    }

    SetImageOverlayStatusBar()
}

@Composable
private fun SetImageOverlayStatusBar() {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent
        )
    }
}

@Composable
private fun PromotionDetailUi(promotionData: PromotionData, back: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val scroll = rememberScrollState()
        Surface(modifier = Modifier.fillMaxSize()) {}
        TopImage(promotionImageUrl(promotionData = promotionData))
        Body(promotionData, scroll)
        Title(promotionData, scroll)
        FilledTonalIconButton(
            onClick = back,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}

@Composable
private fun Title(
    promotionData: PromotionData,
    scroll: ScrollState
) {
    PromotionTitle(
        promotionData = promotionData,
        scroll = scroll
    ) {
        when (promotionData) {
            is VipPromotionData ->
                VipTitleBadge(vipPromotionData = promotionData)
            is StreakPromotionData ->
                StreakPromotionTitle(streakPromotionData = promotionData)
            is HazelPromotionData ->
                HazelPromotionTitle(hazelPromotionData = promotionData)

        }
    }
}

@Composable
fun PromotionTitle(
    promotionData: PromotionData,
    scroll: ScrollState,
    stateIndicator: @Composable () -> Unit = {}
) {
    val maxOffset = with(LocalDensity.current) { MAX_HEADER_OFFSET.toPx() }
    val minOffset = with(LocalDensity.current) { MIN_HEADER_OFFSET.toPx() }

    Column(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .heightIn(min = MIN_HEADER_SIZE)
                .offset {
                    val scrollValue = scroll.value
                    val offset = (maxOffset - scrollValue).coerceAtLeast(minOffset)
                    IntOffset(x = 0, y = offset.toInt())
                }
        ) {
            Surface {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f)
                    ) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            promotionData.promotionName,
                            style = MaterialTheme.typography.headlineLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "TokenId: ${promotionData.shortTokenHash}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                    stateIndicator()
                }
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        }
    }
}

@Composable
private fun Body(promotionData: PromotionData, scroll: ScrollState) {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(MIN_HEADER_OFFSET)
        )
        Spacer(
            modifier = Modifier
                .size(MIN_HEADER_SIZE.minus(5.dp)) // Avoid the background showing through while scrolling
        )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IMAGE_SCROLL)
                )
                Surface {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    PromotionRewardSection(promotionData)
                }
            }
        }
    }
}

@Composable
private fun PromotionRewardSection(promotionData: PromotionData) {
    Text(
        promotionData.promotionDescription,
        style = MaterialTheme.typography.bodyLarge,
        modifier = HzPadding
    )
    when (promotionData) {
        is VipPromotionData -> {
            VipBody(promotionData)
        }
        is HazelPromotionData -> {
            HazelBody(promotionData)
        }
        is StreakPromotionData -> {
            StreakBody(promotionData)
        }
    }
}

@Composable
private fun TopImage(imageUrl: String) {
    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Promotion Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(IMAGE_HEIGHT),
            error = painterResource(id = R.drawable.falllback)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                    )
                )
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HazelPreview() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.hazelPromotionData, {})
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VipPreviewNone() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.vipPromotionData.copy(vipLevel = VipStatus.NONE), {})
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VipPreviewBronze() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.vipPromotionData.copy(vipLevel = VipStatus.BRONZE), {})
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VipPreviewSilver() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.vipPromotionData.copy(vipLevel = VipStatus.SILVER), {})
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VipPreviewGold() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.vipPromotionData.copy(vipLevel = VipStatus.GOLD), {})
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StreakPreview() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.streakPromotionData, {})
    }
}
