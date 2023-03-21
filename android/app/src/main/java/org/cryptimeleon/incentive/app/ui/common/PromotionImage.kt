package org.cryptimeleon.incentive.app.ui.common

import androidx.compose.runtime.Composable
import org.cryptimeleon.incentive.app.BuildConfig
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData

/**
 * Compute url for promotion based on its name, starting from a fixed basepath.
 */
private fun imageUrlSlugForPromotion(promotionData: PromotionData) =
    promotionData.promotionName.lowercase().replace(" ", "_") + ".jpg"

@Composable
fun promotionImageUrl(promotionData: PromotionData): String {
    val basepath = BuildConfig.IMAGE_URL_BASEPATH
    val subpath = imageUrlSlugForPromotion(promotionData)

    return basepath + subpath
}