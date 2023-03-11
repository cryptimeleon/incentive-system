package org.cryptimeleon.incentive.app.ui.basket

import android.content.res.Configuration
import android.icu.text.NumberFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ZkpTokenUpdate
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.ui.log.ZkpSummaryUi
import org.cryptimeleon.incentive.app.ui.preview.PreviewData
import org.cryptimeleon.incentive.app.ui.storeselection.StoreSelectionSheet
import timber.log.Timber
import java.math.BigInteger
import java.util.*

val wrongId: String =
    UUID.randomUUID().toString() // This uuid will never be the id of a basket item
val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)

fun formatCents(valueCents: Int): String = currencyFormat.format(valueCents.toDouble() / 100)

@Composable
fun BasketUi(
    openScanner: () -> Unit,
    openSettings: () -> Unit,
    openBenchmark: () -> Unit,
    openAttacker: () -> Unit,
    gotoRewards: () -> Unit,
    bottomAppBar: @Composable () -> Unit,
) {
    val basketViewModel = hiltViewModel<BasketViewModel>()
    val basket: Basket by basketViewModel.basket.collectAsState(initial = Basket(emptyList(), 0))
    val promotionDataList by basketViewModel.promotionData.collectAsState(initial = emptyList())

    BasketUi(
        basket = basket,
        promotionDataList = promotionDataList,
        setItemCount = basketViewModel::setItemCount,
        setUpdateChoice = basketViewModel::setUpdateChoice,
        pay = gotoRewards,
        openScanner = openScanner,
        openSettings = openSettings,
        openBenchmark = openBenchmark,
        openAttacker = openAttacker,
        discardBasket = basketViewModel::discardCurrentBasket,
        bottomAppBar = bottomAppBar
    )
}

@Composable
private fun BasketUi(
    basket: Basket,
    promotionDataList: List<PromotionData> = emptyList(),
    setItemCount: (String, Int) -> Unit = { _, _ -> },
    pay: () -> Unit = {},
    openScanner: () -> Unit = {},
    openSettings: () -> Unit = {},
    openBenchmark: () -> Unit = {},
    openAttacker: () -> Unit = {},
    discardBasket: () -> Unit = {},
    setUpdateChoice: (BigInteger, TokenUpdate) -> Unit = { _, _ -> },
    bottomAppBar: @Composable () -> Unit = {}
) {
    var showStoreSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = { Text("My Basket") },
                onOpenSettings = openSettings,
                onOpenBenchmark = openBenchmark,
                onOpenAttacker = openAttacker,
                onDiscardBasket = discardBasket,
                onSelectStore = { showStoreSheet = true }
            )
        },
        bottomBar = bottomAppBar
    ) {
        Box(modifier = Modifier.padding(it)) {
            if (basket.items.isEmpty()) {
                BasketEmptyView(openScanner)
            } else {
                BasketNotEmptyView(
                    basket,
                    promotionDataList,
                    setItemCount,
                    setUpdateChoice,
                    pay
                )
            }
        }
    }

    if (showStoreSheet) {
        StoreSelectionSheet(onDismissRequest = { showStoreSheet = false })
    }
}

@Composable
private fun BasketNotEmptyView(
    basket: Basket,
    promotionDataList: List<PromotionData>,
    setItemCount: (String, Int) -> Unit,
    setUpdateChoice: (BigInteger, TokenUpdate) -> Unit,
    pay: () -> Unit
) {
    var expandedBasketItem by remember { mutableStateOf(wrongId) }
    val showLog = remember { mutableStateOf(false) }
    val basketItemsCount = basket.items.sumOf { it.count }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), Arrangement.End
            ) {
                Text(
                    "Price",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.6F
                    )
                )
            }
            basket.items.forEach { item ->
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Divider()
                    BasketItem(
                        item = item,
                        expanded = expandedBasketItem == item.itemId,
                        onClick = {
                            Timber.i("On click ${item.itemId}")
                            expandedBasketItem =
                                if (expandedBasketItem == item.itemId) wrongId else item.itemId
                        },
                        setCount = { count ->
                            setItemCount(item.itemId, count)
                        },
                    )
                }
            }

            promotionDataList.forEach { promotionData: PromotionData ->
                val idString = promotionData.pid.toString()
                if (promotionData.feasibleTokenUpdates.size > 1) {
                    Divider(Modifier.padding(horizontal = 16.dp))
                    TokenUpdateRow(
                        selectedUpdate = promotionData.selectedTokenUpdate,
                        tokenUpdates = promotionData.feasibleTokenUpdates,
                        promotionName = promotionData.promotionName,
                        expanded = expandedBasketItem == idString,
                        onClick = {
                            Timber.i("Onclick $idString")
                            expandedBasketItem =
                                if (expandedBasketItem == idString) wrongId else idString
                        },
                        setSelectedTokenUpdate = { t ->
                            Timber.i("${promotionData.pid} $t")
                            setUpdateChoice(promotionData.pid, t)
                        }
                    )
                    if (showLog.value) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp)
                                .padding(bottom = 18.dp)
                        ) {
                            Column(
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                ZkpSummaryUi(promotionData = promotionData)
                            }
                        }
                    }
                }
            }
            Divider(Modifier.padding(horizontal = 16.dp))
            BasketSummaryRow(
                basketItemsCount = basketItemsCount,
                basket = basket,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(
                modifier = Modifier
                    .height(128.dp)
                    .fillMaxWidth()
            )
        }
        Spacer(
            modifier = Modifier
                .size(16.dp)
        )
        val showDivider by remember {
            derivedStateOf {
                scrollState.value > 0 && scrollState.value < scrollState.maxValue
            }
        }
        val elevation by animateDpAsState(targetValue = if (showDivider) 32.dp else 0.dp)
        Surface(shadowElevation = elevation, modifier = Modifier.align(Alignment.BottomCenter)) {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showLog.value = showLog.value.not() }
                ) {
                    Text((if (showLog.value) "Hide" else "Show") + "Promotion Privacy Details")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = pay
                ) {
                    Text("Checkout")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TokenUpdateRow(
    tokenUpdates: List<TokenUpdate>,
    selectedUpdate: TokenUpdate?,
    promotionName: String,
    expanded: Boolean,
    onClick: () -> Unit,
    setSelectedTokenUpdate: (TokenUpdate) -> Unit = { _ -> },
) {

    Column {
        Surface(
            onClick = onClick,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    Modifier
                        .padding(vertical = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        promotionName,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (selectedUpdate != null) {
                        Text(selectedUpdate.description)
                    } else {
                        Text("Nothing")
                    }
                }
                Crossfade(targetState = expanded) { expanded ->
                    when (expanded) {
                        false -> IconButton(onClick = onClick) {
                            Icon(Icons.Default.ExpandMore, contentDescription = "Expand More")
                        }
                        true -> IconButton(onClick = onClick) {
                            Icon(Icons.Default.ExpandLess, contentDescription = "Expand Less")
                        }
                    }
                }
            }
        }
        val scrollState = rememberScrollState()
        AnimatedVisibility(visible = expanded) {
            Row(Modifier.horizontalScroll(scrollState)) {
                tokenUpdates.forEachIndexed { i, it ->
                    RewardChoiceCard(
                        tokenUpdate = it,
                        selected = selectedUpdate == it,
                        onClick = { setSelectedTokenUpdate(it) },
                        modifier = Modifier
                            .width(200.dp)
                            .padding(8.dp)
                            .padding(start = if (i == 0) 16.dp else 0.dp)
                            .padding(end = if (i == tokenUpdates.lastIndex) 16.dp else 0.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardChoiceCard(
    tokenUpdate: TokenUpdate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = if (selected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 100.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), Arrangement.spacedBy(8.dp)) {
            Text(
                text = tokenUpdate.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (tokenUpdate is ZkpTokenUpdate && tokenUpdate.sideEffect.isPresent) {
                Row {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "Gift icon")
                    Text(
                        text = tokenUpdate.sideEffect.get(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BasketSummaryRow(
    basketItemsCount: Int,
    basket: Basket,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val pluralAppendix = if (basketItemsCount > 1) "s" else ""
        Text(
            text = "Total (${basketItemsCount} Item${pluralAppendix}): ",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = formatCents(basket.value),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun BasketEmptyView(openScanner: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(
            text = "🛒",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 14.em),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Basket is empty!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(onClick = openScanner) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Plus Icon",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Add Items")
        }
    }
}

@Composable
private fun BasketItem(
    item: BasketItem,
    expanded: Boolean,
    onClick: () -> Unit,
    setCount: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize()
            .clickable(onClick = onClick),
    )
    {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "${item.count} x ${item.title}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCents(item.price * item.count),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
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
        Box(
            modifier = Modifier
                .border(
                    1.dp,
                    color = MaterialTheme.colorScheme.secondary,
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
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = "Subtract"
                    )
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = { setCount(count + 1) },
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        tint = MaterialTheme.colorScheme.secondary,
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
    items = testBasketItemList,
    value = 999
)
val emptyTestBasket = Basket(
    items = listOf(),
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
            testBasket,
            promotionDataList = PreviewData.promotionDataList,
        )
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
            emptyTestBasket,
        )
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
