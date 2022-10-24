package org.cryptimeleon.incentive.app.ui.promotion

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipStatus
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData
import org.cryptimeleon.incentive.app.ui.common.promotionImageUrl
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

    setImageOverlayStatusBar()
}

@Composable
private fun setImageOverlayStatusBar() {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromotionDetailUi(promotionData: PromotionData, back: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val scroll = rememberScrollState()
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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        promotionData.promotionName,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        "TokenId: ${promotionData.shortTokenHash}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
                stateIndicator()
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
                .size(MIN_HEADER_SIZE)
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
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                PromotionRewardSection(promotionData)
                Column() {
                    PromotionInfoSectionHeader(text = "Token")
                    Text(
                        text = promotionData.tokenJson,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = HzPadding
                    )
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
@Composable
fun HazelPreview() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.hazelPromotionData, {})
    }
}

@Preview
@Composable
fun VipPreviewNone() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.vipPromotionData.copy(vipLevel = VipStatus.NONE), {})
    }
}

@Preview
@Composable
fun VipPreviewBronze() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.vipPromotionData.copy(vipLevel = VipStatus.BRONZE), {})
    }
}

@Preview
@Composable
fun VipPreviewSilver() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.vipPromotionData.copy(vipLevel = VipStatus.SILVER), {})
    }
}

@Preview
@Composable
fun VipPreviewGold() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.vipPromotionData.copy(vipLevel = VipStatus.GOLD), {})
    }
}

@Preview
@Composable
fun StreakPreview() {
    CryptimeleonPreviewContainer {
        PromotionDetailUi(PreviewData.streakPromotionData, {})
    }
}
