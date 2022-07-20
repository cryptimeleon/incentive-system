package org.cryptimeleon.incentive.app.ui.promotion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipStatus
import org.cryptimeleon.incentive.app.theme.bronze
import org.cryptimeleon.incentive.app.theme.gold
import org.cryptimeleon.incentive.app.theme.onBronze
import org.cryptimeleon.incentive.app.theme.onGold
import org.cryptimeleon.incentive.app.theme.onSilver
import org.cryptimeleon.incentive.app.theme.silver
import java.lang.Float
import kotlin.Boolean
import kotlin.Int
import kotlin.Pair
import kotlin.String

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
                VipLevelBox(
                    "Bronze",
                    promotionData.bronzeScore,
                    MaterialTheme.colorScheme.bronze,
                    MaterialTheme.colorScheme.onBronze,
                    promotionData.vipLevel >= VipStatus.BRONZE
                )
                VipLevelBox(
                    "Silver",
                    promotionData.silverScore,
                    MaterialTheme.colorScheme.silver,
                    MaterialTheme.colorScheme.onSilver,
                    promotionData.vipLevel >= VipStatus.SILVER
                )
                VipLevelBox(
                    "Gold",
                    promotionData.goldScore,
                    MaterialTheme.colorScheme.gold,
                    MaterialTheme.colorScheme.onGold,
                    promotionData.vipLevel >= VipStatus.GOLD
                )
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
                val yTop = Float.max(0f, 2f * level - 3) * py
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
            // Lines first, then circles, to cover lines and avoid addup of semi-transparent colors
            drawLinesToLevel(
                level = 1,
                current = promotionData.score,
                last = 0,
                target = promotionData.bronzeScore
            )
            drawLinesToLevel(
                level = 2,
                current = promotionData.score,
                last = promotionData.bronzeScore,
                target = promotionData.silverScore
            )
            drawLinesToLevel(
                level = 3,
                current = promotionData.score,
                last = promotionData.silverScore,
                target = promotionData.goldScore
            )
            drawCirclesToLevel(
                level = 1,
                current = promotionData.score,
                target = promotionData.bronzeScore
            )
            drawCirclesToLevel(
                level = 2,
                current = promotionData.score,
                target = promotionData.silverScore
            )
            drawCirclesToLevel(
                level = 3,
                current = promotionData.score,
                target = promotionData.goldScore
            )
        }
    }
}

@Composable
private fun VipLevelBox(
    name: String,
    requiredScore: Int,
    color: Color,
    textColor: Color,
    active: Boolean
) {
    val alpha = if (active) 1.0f else 0.4f
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(
                "$name VIP",
                style = MaterialTheme.typography.headlineSmall,
                color = textColor
            )
            Text(
                "$requiredScore Points",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}

@Composable
fun VipTitleBadge(vipPromotionData: VipPromotionData) {
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
