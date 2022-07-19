package org.cryptimeleon.incentive.app.ui.promotion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.ProveVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.UpgradeVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipStatus
import org.cryptimeleon.incentive.app.theme.bronze
import org.cryptimeleon.incentive.app.theme.gold
import org.cryptimeleon.incentive.app.theme.onBronze
import org.cryptimeleon.incentive.app.theme.onGold
import org.cryptimeleon.incentive.app.theme.onSilver
import org.cryptimeleon.incentive.app.theme.silver
import org.cryptimeleon.incentive.app.ui.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData
import java.lang.Float.max
import java.math.BigInteger

val IMAGE_HEIGHT = 280.dp
val IMAGE_SCROLL = 140.dp

val MIN_HEADER_OFFSET = 56.dp
val MAX_HEADER_OFFSET = MIN_HEADER_OFFSET + IMAGE_SCROLL
val MIN_HEADER_SIZE = 100.dp


val CREDIT_CARD_HEIGHT = 60.dp

private val HzPadding = Modifier.padding(horizontal = 16.dp)


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
        TopImage(promotionData.promotionImageUrl)
        Body(promotionData, scroll)
        when (promotionData) {
            is VipPromotionData -> {
                VipStateHeader(vipPromotionData = promotionData) { scroll.value }
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
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
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                when (promotionData) {
                    is VipPromotionData -> {
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            promotionData.promotionDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = HzPadding
                        )
                        Text(
                            "Points: ${promotionData.score}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = HzPadding
                        )
                        Text(
                            "Progress",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = HzPadding.padding(vertical = 8.dp),
                        )
                        VipProgressBox(
                            promotionData,
                        )
                        Text(
                            "Rewards",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = HzPadding.padding(vertical = 8.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            promotionData.tokenUpdates.forEach { tokenUpdate ->
                                when (tokenUpdate) {
                                    is NoTokenUpdate -> {}
                                    is EarnTokenUpdate -> {}
                                    is UpgradeVipTokenUpdateState -> {
                                        if (tokenUpdate.targetVipStatus.statusValue > tokenUpdate.currentVipStatus.statusValue) {
                                            Card(modifier = HzPadding.fillMaxWidth()) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text("Become ${tokenUpdate.targetVipStatus}")
                                                    Text("Requires ${tokenUpdate.requiredPoints}. Your have ${tokenUpdate.currentPoints}")
                                                    Text(tokenUpdate.description)
                                                }
                                            }
                                        }
                                    }
                                    is ProveVipTokenUpdateState -> {
                                        if (tokenUpdate.currentStatus == tokenUpdate.requiredStatus) {
                                            Card(modifier = HzPadding.fillMaxWidth()) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text("Your current Bonus")
                                                    Text(tokenUpdate.description)
                                                }
                                            }
                                        } else if (tokenUpdate.currentStatus.statusValue < tokenUpdate.requiredStatus.statusValue) {
                                            Card(modifier = HzPadding.fillMaxWidth()) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text("Requires VipLevel ${tokenUpdate.requiredStatus}")
                                                    Text(tokenUpdate.description)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            "Token",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = HzPadding
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = promotionData.tokenJson,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = HzPadding
                        )
                    }
                    else -> {
                        Text(
                            promotionData.promotionName,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VipProgressBox(promotionData: VipPromotionData) {
    Box(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(fraction = .85f)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .height(250.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.bronze,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            "Bronze VIP",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBronze
                        )
                        Text(
                            "5000 Points",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBronze
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.silver,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.4f)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            "Silver VIP",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSilver
                        )
                        Text(
                            "10000 Points",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSilver
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.gold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.4f)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            "Gold VIP",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onGold
                        )
                        Text(
                            "20000 Points",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onGold
                        )
                    }
                }
            }

        }
        val colorEnabled = MaterialTheme.colorScheme.primary
        val colorDisabled = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        // For avoiding adding of transparent objects
        val bgColor = MaterialTheme.colorScheme.background
        val lineWidthEnabled = 8f
        val lineWidthDisabled = 6f
        val radiusEnabled = 16f
        val radiusDisabled = 14f

        Canvas(
            modifier = Modifier
                .fillMaxWidth(fraction = .15f)
                .fillMaxHeight()
        ) {
            val py = size.height / 6f
            val px = size.width

            // Starting point
            drawCircle(color = colorEnabled, radiusEnabled, Offset(0f, 0f))

            fun drawLinesToLevel(level: Int, current: Int, last: Int, target: Int) {
                val yTop = max(0f, 2f * level - 3) * py
                val yBottom = (2f * level - 1) * py
                val enabled = current >= target
                val cutInHalf = !enabled && current > last
                if (enabled) {
                    drawLine(
                        color = colorEnabled,
                        start = Offset(0f, yTop),
                        end = Offset(0f, yBottom),
                        strokeWidth = lineWidthEnabled,
                    )
                } else if (cutInHalf) {
                    val percentageOfLevel = 1f * (target - current) / (target - last)
                    val yEnabled = yTop + percentageOfLevel * (yBottom - yTop)
                    drawLine(
                        color = colorEnabled,
                        start = Offset(0f, yTop),
                        end = Offset(0f, yEnabled),
                        strokeWidth = lineWidthEnabled,
                    )
                    drawLine(
                        color = colorDisabled,
                        start = Offset(0f, yEnabled),
                        end = Offset(0f, yBottom),
                        strokeWidth = lineWidthDisabled,
                    )
                } else {
                    drawLine(
                        color = colorDisabled,
                        start = Offset(0f, yTop),
                        end = Offset(0f, yBottom),
                        strokeWidth = lineWidthDisabled,
                    )
                }
                drawLine(
                    color = if (enabled) colorEnabled else colorDisabled,
                    start = Offset(0f, yBottom),
                    end = Offset(px, yBottom),
                    strokeWidth = if (enabled) lineWidthEnabled else lineWidthDisabled,
                )
            }

            fun drawCirclesToLevel(level: Int, current: Int, target: Int) {
                val yBottom = (2f * level - 1) * py
                val enabled = current >= target
                // Overwrite lines for transparency addup reasons
                drawCircle(
                    color = bgColor,
                    radius = if (enabled) radiusEnabled else radiusDisabled,
                    center = Offset(0f, yBottom)
                )
                drawCircle(
                    color = if (enabled) colorEnabled else colorDisabled,
                    radius = if (enabled) radiusEnabled else radiusDisabled,
                    center = Offset(0f, yBottom)
                )
            }
            drawLinesToLevel(1, 8_745, 0, 5_000)
            drawLinesToLevel(2, 8_745, 5_000, 10_000)
            drawLinesToLevel(3, 8_745, 10_000, 20_000)
            drawCirclesToLevel(1, 8_745, 0)
            drawCirclesToLevel(2, 8_745, 10_000)
            drawCirclesToLevel(3, 8_745, 20_000)
        }
    }
}

@Composable
private fun VipStateHeader(vipPromotionData: VipPromotionData, scrollProvider: () -> Int) {
    val maxOffset = with(LocalDensity.current) { MAX_HEADER_OFFSET.toPx() }
    val minOffset = with(LocalDensity.current) { MIN_HEADER_OFFSET.toPx() }

    Column(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {

        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .heightIn(min = MIN_HEADER_SIZE)
                .offset {
                    val scroll = scrollProvider()
                    val offset = (maxOffset - scroll).coerceAtLeast(minOffset)
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
                        vipPromotionData.promotionName,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        "TokenId: ${vipPromotionData.shortTokenHash}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    val (color, textcolor) = when (vipPromotionData.vipLevel) {
                        VipStatus.BRONZE -> Pair(
                            MaterialTheme.colorScheme.bronze,
                            MaterialTheme.colorScheme.onBronze
                        )
                        VipStatus.SILVER -> Pair(
                            MaterialTheme.colorScheme.silver,
                            MaterialTheme.colorScheme.onSilver
                        )
                        VipStatus.GOLD -> Pair(
                            MaterialTheme.colorScheme.gold,
                            MaterialTheme.colorScheme.onGold
                        )
                        else -> Pair(Color.Gray, Color.White)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(CREDIT_CARD_HEIGHT)
                            .aspectRatio(1.5857f)
                            //.border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(color)
                    ) {
                        Text(
                            vipPromotionData.vipLevel.toString(),
                            Modifier.padding(8.dp),
                            color = textcolor
                        )
                    }
                }
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
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
                .height(IMAGE_HEIGHT)
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
