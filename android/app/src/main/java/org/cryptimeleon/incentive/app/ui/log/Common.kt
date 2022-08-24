package org.cryptimeleon.incentive.app.ui.log

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em


const val GEQ = "≥"
const val LEQ = "≤"

@Composable
fun NothingText() {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.bodySmall.toSpanStyle()
                    .copy(fontFamily = FontFamily.Monospace)
            ) {
                storesView()
                append("Choice: \"Nothing\"")
            }
        },
        inlineContent = arrowInlineContent
    )
}

fun pluralS(count: Int) = if (count > 1) "s" else ""

fun AnnotatedString.Builder.yourView() {
    pushStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold))
    append("Data only you know:\n")
    pop()
}

fun AnnotatedString.Builder.storesView() {
    pushStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold))
    append("Store's view:\n")
    pop()
}

fun AnnotatedString.Builder.arrow() {
    appendInlineContent("arrow", "->")
}

val arrowInlineContent = mapOf(
    Pair(
        "arrow",
        InlineTextContent(
            Placeholder(
                width = 1.0.em,
                height = 1.0.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Icon(Icons.Default.TrendingFlat, contentDescription = "Arrow")
        }
    )
)
