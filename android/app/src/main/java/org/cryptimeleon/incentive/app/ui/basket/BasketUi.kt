package org.cryptimeleon.incentive.app.ui.basket

import android.content.res.Configuration
import android.icu.text.NumberFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ZkpTokenUpdate
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import org.cryptimeleon.incentive.app.util.SLE
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
) {
    val basketViewModel = hiltViewModel<BasketViewModel>()
    val basket: SLE<Basket> by basketViewModel.basket.collectAsState(initial = SLE.Loading())
    val promotionDataList by basketViewModel.promotionData.collectAsState(initial = emptyList())

    BasketUi(
        basketSle = basket,
        promotionDataList = promotionDataList,
        setItemCount = basketViewModel::setItemCount,
        setUpdateChoice = basketViewModel::setUpdateChoice,
        pay = gotoRewards,
        openScanner = openScanner,
        openSettings = openSettings,
        openBenchmark = openBenchmark,
        openAttacker = openAttacker
    )
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun BasketUi(
    basketSle: SLE<Basket>,
    promotionDataList: List<PromotionData> = emptyList(),
    setItemCount: (String, Int) -> Unit = { _, _ -> },
    pay: () -> Unit = {},
    openScanner: () -> Unit = {},
    openSettings: () -> Unit = {},
    openBenchmark: () -> Unit = {},
    openAttacker: () -> Unit = {},
    setUpdateChoice: (BigInteger, TokenUpdate) -> Unit = { _, _ -> }
) {

    Scaffold(topBar = {
        DefaultTopAppBar(
            title = { Text("My Basket") },
            onOpenSettings = openSettings,
            onOpenBenchmark = openBenchmark,
            onOpenAttacker = openAttacker
        )
    }) {
        Box(modifier = Modifier.padding(it)) {
            when (basketSle) {
                is SLE.Error -> Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Error, no basket!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Ensure you have an active internet connection and restart the app!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // TODO refresh button?
                }
                is SLE.Loading -> LoadingSpinner()
                is SLE.Success -> {
                    val basket = basketSle.data!!
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
        }
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
    val basketItemsCount = basket.items.map { it.count }.sum()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    Text(
                        "Price",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.6F
                        )
                    )
                }
            }
            items(basket.items) { item ->
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

            promotionDataList.forEach { promotionData: PromotionData ->
                val idString = promotionData.pid.toString()
                if (promotionData.feasibleTokenUpdates.size > 1) {
                    item(key = promotionData.pid) {
                        Divider()
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
                    }
                }
            }
            item {
                Divider()
                BasketSummaryRow(basketItemsCount, basket)
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
        Column() {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = pay
            ) {
                Text("Checkout")
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TokenUpdateRow(
    tokenUpdates: List<TokenUpdate>,
    selectedUpdate: TokenUpdate?,
    promotionName: String,
    expanded: Boolean,
    onClick: () -> Unit,
    setSelectedTokenUpdate: (TokenUpdate) -> Unit = { _ -> },
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    SideEffect {
        scope.launch {
            selectedUpdate?.let {
                val index = tokenUpdates.indexOf(it)
                if (index != -1) {
                    pagerState.scrollToPage(index)
                }
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.drop(1).collect { page ->
            setSelectedTokenUpdate(tokenUpdates[page])
        }
    }

    Column {
        Surface(
            onClick = onClick,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                    selectedUpdate?.let {
                        Text(it.description)
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
        AnimatedVisibility(visible = expanded) {
            HorizontalPager(
                count = tokenUpdates.size,
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 64.dp)
            ) { i ->
                val tokenUpdate = tokenUpdates[i]
                RewardChoiceCard(
                    tokenUpdate = tokenUpdate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}


@Composable
private fun RewardChoiceCard(
    tokenUpdate: TokenUpdate,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        // colors = if (choice.isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
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
    basket: Basket
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
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
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
        )
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
        )
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
            SLE.Success(emptyTestBasket),
        )
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
@Preview(
    uiMode = previewUiMode,
    showBackground = true,
    name = "Error Basket"
)
private fun BasketPreviewError() {
    CryptimeleonTheme {
        BasketUi(SLE.Error())
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
