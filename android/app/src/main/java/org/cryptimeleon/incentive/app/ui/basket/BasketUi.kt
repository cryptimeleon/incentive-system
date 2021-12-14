package org.cryptimeleon.incentive.app.ui.basket

import android.content.res.Configuration
import android.icu.text.NumberFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
fun BasketUi(openSettings: () -> Unit, openBenchmark: () -> Unit) {
    val basketViewModel = hiltViewModel<BasketViewModel>()
    val basket: SLE<Basket> by basketViewModel.basket.collectAsState(initial = SLE.Loading())

    BasketUi(
        basketSle = basket,
        setItemCount = basketViewModel::setItemCount,
        pay = basketViewModel::payAndRedeem,
        discard = basketViewModel::onDiscardClicked,
        openSettings = openSettings,
        openBenchmark = openBenchmark
    )
}

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
    val lazyListState = rememberLazyListState()

    Scaffold(topBar = {
        DefaultTopAppBar(
            onOpenSettings = openSettings,
            onOpenBenchmark = openBenchmark
        )
    }) {
        when (basketSle) {
            is SLE.Error -> TODO()
            is SLE.Loading -> LoadingSpinner()
            is SLE.Success -> Column(
                modifier = Modifier
                    .fillMaxHeight(),
            ) {
                val basket = basketSle.data!!
                if (basket.items.isNotEmpty()) {
                    val basketItemsCount = basket.items.map { it.count }.sum()
                    Text(
                        text = "$basketItemsCount Item${if (basketItemsCount > 1) "s" else ""} in your cart",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.body1
                    )
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp, bottom = 8.dp),
                        state = lazyListState
                    ) {
                        items(basket.items) { item ->
                            BasketItem(
                                item = item,
                                expanded = expandedBasketItem == item.itemId,
                                onClick = {
                                    expandedBasketItem =
                                        if (expandedBasketItem == item.itemId) wrongId else item.itemId
                                }
                            ) { count ->
                                setItemCount(item.itemId, count)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = "Total: ${formatCents(basket.value)}",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { discard() }
                        ) {
                            Text("Discard")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { pay() }
                        ) {
                            Text("Pay and Redeem")
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BasketItem(
    item: BasketItem,
    expanded: Boolean = false,
    onClick: () -> Unit,
    setCount: (Int) -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize()
            .clickable(onClick = onClick),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.weight(1f)
                )
                Column {
                    Text(
                        text = formatCents(item.price * item.count),
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Text(
                        text = "${item.count} x ${formatCents(item.price)}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier
                            .alpha(0.8f)
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
            Icon(Icons.Outlined.Delete, "Delete")
        }
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .border(
                    1.dp,
                    color = MaterialTheme.colors.onBackground,
                    shape = RoundedCornerShape(16.dp)
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { setCount(count - 1) },
                ) {
                    Icon(Icons.Outlined.Remove, "Subtract")
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.subtitle1
                )
                IconButton(
                    onClick = { setCount(count + 1) },
                ) {
                    Icon(Icons.Outlined.Add, "Add")
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
const val previewUiMode = Configuration.UI_MODE_NIGHT_YES

@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "Basket Preview"
)
private fun BasketPreview() {
    CryptimeleonTheme {
        BasketUi(SLE.Success(testBasket), { _, _ -> {} }, {}, {})
    }
}

@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "Loading preview"
)
private fun BasketPreviewLoading() {
    CryptimeleonTheme {
        BasketUi(SLE.Loading(), { _, _ -> {} }, {}, {})
    }
}

@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "Empty Basket"
)
private fun BasketPreviewEmpty() {
    CryptimeleonTheme {
        BasketUi(SLE.Success(emptyTestBasket), { _, _ -> {} }, {}, {})
    }
}

@Composable
private fun BasketItemPreview(expanded: Boolean) {
    CryptimeleonTheme {
        BasketItem(testBasketItemList[0], expanded, {}) {}
    }
}

@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "BasketItem normal"
)
fun BasketItemPreview() {
    BasketItemPreview(false)
}

@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "BasketItem expanded"
)
fun BasketItemPreviewExpanded() {
    BasketItemPreview(true)
}
