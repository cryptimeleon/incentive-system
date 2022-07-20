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
import androidx.compose.material3.Card
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
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.ProveVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StreakDate
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.UpgradeVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipStatus
import org.cryptimeleon.incentive.app.ui.CryptimeleonPreviewContainer
import org.cryptimeleon.incentive.app.ui.preview.PreviewData
import java.math.BigInteger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

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
    when (promotionData) {
        is VipPromotionData -> VipPromotionTitle(
            vipPromotionData = promotionData,
            scroll = scroll
        )
        is StreakPromotionData -> StreakPromotionTitle(
            streakPromotionData = promotionData,
            scroll = scroll
        )
        is HazelPromotionData -> HazelPromotionTitle(
            hazelPromotionData = promotionData,
            scroll = scroll
        )
    }
}

@Composable
fun VipPromotionTitle(vipPromotionData: VipPromotionData, scroll: ScrollState) =
    PromotionTitle(promotionData = vipPromotionData, { scroll.value }) {
        VipTitleBadge(vipPromotionData = vipPromotionData)
    }

@Composable
fun StreakPromotionTitle(streakPromotionData: StreakPromotionData, scroll: ScrollState) =
    PromotionTitle(promotionData = streakPromotionData, { scroll.value }) {
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
fun HazelPromotionTitle(hazelPromotionData: HazelPromotionData, scroll: ScrollState) =
    PromotionTitle(promotionData = hazelPromotionData, { scroll.value }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Points: ${hazelPromotionData.score}")
        }
    }

@Composable
fun PromotionTitle(
    promotionData: PromotionData,
    scrollProvider: () -> Int,
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
                        VipProgressBox(promotionData)
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
