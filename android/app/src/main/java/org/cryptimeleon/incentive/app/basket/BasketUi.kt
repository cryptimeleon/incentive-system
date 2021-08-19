package org.cryptimeleon.incentive.app.basket

import android.content.res.Configuration
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.data.network.Item
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme

@Composable
fun Basket(openSettings: () -> Unit, openBenchmark: () -> Unit) {
    val basketViewModel = hiltViewModel<BasketViewModel>()
    val allBasketItems by basketViewModel.basketContent.observeAsState(emptyList())
    val basket by basketViewModel.basket.observeAsState()
    val basketValue by basketViewModel.basketValue.observeAsState("")

    basket?.let {
        Basket(
            allBasketItems,
            basketValue,
            setItemCount = basketViewModel::setItemCount,
            pay = basketViewModel::payAndRedeem,
            discard = basketViewModel::onDiscardClicked,
            openSettings = openSettings,
            openBenchmark = openBenchmark
        )
    }

}

@Composable
private fun Basket(
    allBasketItems: List<BasketListItem>,
    basketValue: String,
    setItemCount: (String, Int) -> Unit,
    pay: () -> Unit,
    discard: () -> Unit,
    openSettings: () -> Unit = {},
    openBenchmark: () -> Unit = {}
) {
    var expandedBasketItem by remember { mutableStateOf<String?>(null) }
    val lazyListState = rememberLazyListState()

    Scaffold(topBar = {
        DefaultTopAppBar(
            onOpenSettings = openSettings,
            onOpenBenchmark = openBenchmark
        )
    }) {
        Column(
            modifier = Modifier
                .fillMaxHeight(),
        ) {
            if (allBasketItems.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp, bottom = 8.dp),
                    state = lazyListState
                ) {
                    items(allBasketItems) { basketListItem ->
                        BasketItem(
                            basketListItem = basketListItem,
                            expanded = expandedBasketItem == basketListItem.item.id,
                            onClick = {
                                expandedBasketItem =
                                    if (expandedBasketItem == basketListItem.item.id) null else basketListItem.item.id
                            },
                            { count ->
                                setItemCount(basketListItem.item.id, count)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = "Total: $basketValue",
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


@Composable
private fun BasketItem(
    basketListItem: BasketListItem,
    expanded: Boolean = false,
    onClick: () -> Unit,
    setCount: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    basketListItem.item.title,
                    style = MaterialTheme.typography.h5
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "${basketListItem.count} x ${basketListItem.priceSingle}",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }
                    Text(
                        text = basketListItem.priceTotal,
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
            if (expanded) {
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
                                onClick = { setCount(basketListItem.count - 1) },
                            ) {
                                Icon(Icons.Outlined.Remove, "Subtract")
                            }
                            Text(
                                text = basketListItem.countStr,
                                style = MaterialTheme.typography.subtitle1
                            )
                            IconButton(
                                onClick = { setCount(basketListItem.count + 1) },
                            ) {
                                Icon(Icons.Outlined.Add, "Add")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun BasketPreview() {
    CryptimeleonTheme {
        val basketItemList = remember {
            listOf(
                BasketListItem(
                    Item(
                        "Some id",
                        199,
                        "Apple"
                    ), 8
                ),
                BasketListItem(
                    Item(
                        "Some other id",
                        89999,
                        "Ipad with a really, really long title !!!!!!!!!!"
                    ), 1
                )
            )
        }
        Basket(basketItemList, "999.99€", { _, _ -> {} }, {}, {})
    }
}

@Composable
@Preview(
    showBackground = true,
    name = "Basket in Light Mode"
)
private fun BasketPreviewEmptyLight() {
    CryptimeleonTheme() {
        Basket(emptyList(), "", { _, _ -> {} }, {}, {})
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Basket in Dark Mode"
)
private fun BasketPreviewEmptyDark() {
    CryptimeleonTheme() {
        Basket(emptyList(), "", { _, _ -> {} }, {}, {})
    }
}

@Composable
@Preview(
    showBackground = true,
    name = "Basket in Light Mode"
)
private fun BasketPreviewLight() {
    BasketPreview()
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Basket in Dark Mode"
)
private fun BasketPreviewDark() {
    BasketPreview()
}


@Composable
private fun BasketItemPreview(expanded: Boolean = false) {
    CryptimeleonTheme {
        val basketItem = remember {
            BasketListItem(
                Item(
                    "Some id",
                    199,
                    "Apple"
                ), 8
            )
        }
        BasketItem(basketItem, expanded, {}, {})
    }
}

@Preview
@Composable
fun BasketItemPreviewLight() {
    BasketItemPreview()
}

@Preview
@Composable
fun BasketItemPreviewExpandedLight() {
    BasketItemPreview(true)
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
fun BasketItemPreviewDark() {
    BasketItemPreview()
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
fun BasketItemPreviewExpandedDark() {
    BasketItemPreview(true)
}
