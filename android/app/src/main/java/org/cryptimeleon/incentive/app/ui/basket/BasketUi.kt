package org.cryptimeleon.incentive.app.ui.basket

import android.content.res.Configuration
import android.icu.text.NumberFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.util.SLE
import java.util.*

val wrongId: String =
    UUID.randomUUID().toString() // This uuid will never be the id of a basket item
val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)

fun formatCents(valueCents: Int): String = currencyFormat.format(valueCents.toDouble() / 100)

@Composable
fun BasketUi(openSettings: () -> Unit, openBenchmark: () -> Unit, gotoRewards: () -> Unit) {
    val basketViewModel = hiltViewModel<BasketViewModel>()
    val basket: SLE<Basket> by basketViewModel.basket.collectAsState(initial = SLE.Loading())

    BasketUi(
        basketSle = basket,
        setItemCount = basketViewModel::setItemCount,
        pay = gotoRewards,
        discard = basketViewModel::onDiscardClicked,
        openSettings = openSettings,
        openBenchmark = openBenchmark
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun BasketUi(
    basketSle: SLE<Basket>,
    setItemCount: (String, Int) -> Unit,
    pay: () -> Unit,
    discard: () -> Unit,
    openSettings: () -> Unit = {},
    openBenchmark: () -> Unit = {}
) {
    var expandedBasketItem by remember { mutableStateOf(wrongId) }

    Scaffold(topBar = {
        DefaultTopAppBar(
            onOpenSettings = openSettings,
            onOpenBenchmark = openBenchmark
        )
    }) {
        when (basketSle) {
            is SLE.Error -> TODO()
            is SLE.Loading -> LoadingSpinner()
            is SLE.Success -> {
                val basket = basketSle.data!!
                if (basket.items.isNotEmpty()) {
                    val basketItemsCount = basket.items.map { it.count }.sum()
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            item {
                                Row() {
                                    AnimatedContent(
                                        targetState = basketItemsCount,
                                        transitionSpec = {
                                            if (targetState > initialState) {
                                                slideInVertically { height -> height } + fadeIn() with
                                                        slideOutVertically { height -> -height } + fadeOut()
                                            } else {
                                                slideInVertically { height -> -height } + fadeIn() with
                                                        slideOutVertically { height -> height } + fadeOut()
                                            }.using(
                                                SizeTransform(clip = false)
                                            )
                                        }) { targetItem ->
                                        Text(
                                            text = "$targetItem",
                                            style = MaterialTheme.typography.overline,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Text(
                                        text = " Item${if (basketItemsCount > 1) "s" else ""} in your cart:",
                                        style = MaterialTheme.typography.overline,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            items(basket.items) { item ->
                                Divider()
                                BasketItem(
                                    item = item,
                                    expanded = expandedBasketItem == item.itemId,
                                    onClick = {
                                        expandedBasketItem =
                                            if (expandedBasketItem == item.itemId) wrongId else item.itemId
                                    },
                                    setCount = { count ->
                                        setItemCount(item.itemId, count)
                                    },
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                            item {
                                Divider(thickness = 3.dp)
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Total",
                                        style = MaterialTheme.typography.h5,
                                    )
                                    Text(
                                        text = formatCents(basket.value),
                                        style = MaterialTheme.typography.h5,
                                    )
                                }
                                Spacer(
                                    modifier = Modifier
                                        .size(32.dp)
                                )
                            }
                            item {
                                Spacer(
                                    modifier = Modifier
                                        .size(32.dp)
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = discard
                            ) {
                                Text("Discard")
                            }
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { pay() }
                            ) {
                                Text("Checkout")
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Basket is empty!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Text(
                            text = "Open the scanner to items to your basket!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingSpinner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Loading basket...",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun BasketItem(
    item: BasketItem,
    expanded: Boolean,
    onClick: () -> Unit,
    setCount: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize()
            .clickable(onClick = onClick),
    )
    {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCents(item.price * item.count),
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .align(Alignment.End)
                    )
                    Text(
                        text = "${item.count} x ${formatCents(item.price)}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier
                            .align(Alignment.End)
                    )
                }
            }
            if (expanded) {
                BasketItemControlRow(setCount, item.count)
            }
        }
    }
}


@Composable
private fun BasketItemControlRow(
    setCount: (Int) -> Unit,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = { setCount(0) },
        ) {
            Icon(
                Icons.Outlined.Delete,
                tint = MaterialTheme.colors.secondary,
                contentDescription = "Delete"
            )
        }
        Box(
            modifier = Modifier
                .border(
                    1.dp,
                    color = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(16.dp)
                )
                .wrapContentWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { setCount(count - 1) },
                ) {
                    Icon(
                        Icons.Outlined.Remove,
                        tint = MaterialTheme.colors.secondary,
                        contentDescription = "Subtract"
                    )
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.subtitle1
                )
                IconButton(
                    onClick = { setCount(count + 1) },
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        tint = MaterialTheme.colors.secondary,
                        contentDescription = "Add"
                    )
                }
            }
        }
    }
}


// Ui previews

val testBasketItemList =
    listOf(
        BasketItem(
            UUID.randomUUID().toString(),
            "Apple",
            199,
            8
        ),
        BasketItem(
            UUID.randomUUID().toString(),
            "Ipad with a really, really long title !!!!!!!!!!",
            89999,
            1
        )
    )
val testBasket = Basket(
    UUID.randomUUID(), testBasketItemList,
    paid = false,
    redeemed = false,
    value = 999
)
val emptyTestBasket = Basket(
    UUID.randomUUID(), listOf(),
    paid = false,
    redeemed = false,
    value = 91591
)
const val previewUiMode = Configuration.UI_MODE_NIGHT_NO

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "Basket Preview"
)
private fun BasketPreview() {
    CryptimeleonTheme {
        BasketUi(
            SLE.Success(testBasket),
            { _, _ -> },
            {},
            {},
            {})
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "Loading preview"
)
private fun BasketPreviewLoading() {
    CryptimeleonTheme {
        BasketUi(
            SLE.Loading(),
            { _, _ -> },
            {},
            {},
            {})
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "Empty Basket"
)
private fun BasketPreviewEmpty() {
    CryptimeleonTheme {
        BasketUi(
            SLE.Success(emptyTestBasket),
            { _, _ -> },
            {},
            {},
            {})
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
private fun BasketItemPreview(expanded: Boolean) {
    CryptimeleonTheme {
        BasketItem(testBasketItemList[0], expanded, {}, {})
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "BasketItem normal"
)
fun BasketItemPreview() {
    BasketItemPreview(false)
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "BasketItem expanded"
)
fun BasketItemPreviewExpanded() {
    BasketItemPreview(true)
}
