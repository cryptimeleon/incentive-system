package org.cryptimeleon.incentive.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData

/**
 * Compute url for promotion based on its name, starting from a fixed basepath.
 */
private fun imageUrlSlugForPromotion(promotionData: PromotionData) =
    promotionData.promotionName.lowercase().replace(" ", "_") + ".jpg"

@Composable
fun promotionImageUrl(promotionData: PromotionData): String {
    val basepath = stringResource(id = R.string.image_url_basepath)
    val subpath = imageUrlSlugForPromotion(promotionData)

    return basepath + subpath
}