package org.cryptimeleon.incentive.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.cryptimeleon.incentive.app.R

private val oswaldExtralight = Font(R.font.oswald_extralight, FontWeight.W200)
private val oswaldLight = Font(R.font.oswald_light, FontWeight.W300)
private val oswaldRegular = Font(R.font.oswald_regular, FontWeight.W400)
private val oswaldMedium = Font(R.font.oswald_medium, FontWeight.W500)
private val oswaldSemibold = Font(R.font.oswald_semibold, FontWeight.W600)
private val oswaldBold = Font(R.font.oswald_bold, FontWeight.W700)

private val robotoCondensedLight = Font(R.font.roboto_condensed_light, FontWeight.W300)
private val robotoCondensedLightItalic =
    Font(R.font.roboto_condensed_light, FontWeight.W300, FontStyle.Italic)
private val robotoCondensedRegular = Font(R.font.roboto_condensed_regular, FontWeight.W400)
private val robotoCondensedRegularItalic =
    Font(R.font.roboto_condensed_regular_italic, FontWeight.W400, FontStyle.Italic)
private val robotoCondensedBold = Font(R.font.roboto_condensed_bold, FontWeight.W700)
private val robotoCondensedBoldItalic =
    Font(R.font.roboto_condensed_bold_italic, FontWeight.W700, FontStyle.Italic)
private val headingFontFamily =
    FontFamily(
        fonts = listOf(
            oswaldExtralight,
            oswaldLight,
            oswaldRegular,
            oswaldMedium,
            oswaldSemibold,
            oswaldBold
        )
    )

private val textFontFamily = FontFamily(
    fonts = listOf(
        robotoCondensedLight,
        robotoCondensedLightItalic,
        robotoCondensedRegular,
        robotoCondensedRegularItalic,
        robotoCondensedBold,
        robotoCondensedBoldItalic
    )
)

val typography = Typography(
    defaultFontFamily = textFontFamily,
    h1 = TextStyle(
        fontFamily = headingFontFamily,
        fontSize = 89.sp,
        fontWeight = FontWeight.W300,
        letterSpacing = (-1.5).sp
    ),
    h2 = TextStyle(
        fontFamily = headingFontFamily,
        fontSize = 55.sp,
        fontWeight = FontWeight.W300,
        letterSpacing = (-0.5).sp
    ),
    h3 = TextStyle(fontFamily = headingFontFamily, fontSize = 44.sp, fontWeight = FontWeight.W400),
    h4 = TextStyle(
        fontFamily = headingFontFamily,
        fontSize = 31.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 0.25.sp
    ),
    h5 = TextStyle(fontFamily = headingFontFamily, fontSize = 22.sp, fontWeight = FontWeight.W400),
    h6 = TextStyle(
        fontFamily = headingFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.W500
    ), // Title Bar
    subtitle1 = TextStyle(
        fontFamily = headingFontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 0.15.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = headingFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 0.1.sp
    ),
    body1 = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 0.5.sp
    ),
    body2 = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 0.25.sp
    ),
    button = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 1.25.sp,
        fontFeatureSettings = "c2sc, smcp"
    ),
    caption = TextStyle(
        fontFamily = headingFontFamily, // roboto,12,400,0.4
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),
    overline = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.W400,
        letterSpacing = 1.5.sp,
        fontFeatureSettings = "c2sc, smcp"
    )

)


private val DarkColors = darkColors(
    primary = md_theme_dark_primary,
    secondary = md_theme_dark_secondary,
    surface = md_theme_dark_surface,
    onPrimary = md_theme_dark_onPrimary,
    onSecondary = md_theme_dark_onSecondary,
    onSurface = md_theme_dark_onSurface,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
)

private val LightColors = lightColors(
    primary = md_theme_light_primary,
    secondary = md_theme_light_secondary,
    surface = md_theme_light_surface,
    onPrimary = md_theme_light_onPrimary,
    onSecondary = md_theme_light_onSecondary,
    onSurface = md_theme_light_onSurface,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
)

@Composable
fun CryptimeleonTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = typography,
        content = content
    )
}
