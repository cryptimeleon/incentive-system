package org.cryptimeleon.incentive.app.ui.scan

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Rect
import android.view.ViewTreeObserver
import android.widget.NumberPicker
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import org.cryptimeleon.incentive.app.ui.common.DefaultTopAppBar
import timber.log.Timber
import androidx.camera.core.Preview as CameraPreview

@Composable
fun ScanScreen(openSettings: () -> Unit, openBenchmark: () -> Unit, openAttacker: () -> Unit) {
    val viewModel = hiltViewModel<ScanViewModel>()
    val state by viewModel.state.observeAsState(ScanEmptyState)
    val filter by viewModel.itemFilter.collectAsState()
    val filteredItems by viewModel.itemsFlow.collectAsState(emptyList())

    ScannerScreen(
        openBenchmark,
        openSettings,
        openAttacker,
        viewModel::onAmountChange,
        viewModel::onAddToBasket,
        viewModel::onDiscardItem,
        viewModel::setBarcode,
        state,
        filter,
        filteredItems,
        viewModel::setFilter
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun ScannerScreen(
    openBenchmark: () -> Unit,
    openSettings: () -> Unit,
    openAttacker: () -> Unit,
    onAmountChange: (Int) -> Unit,
    onAddToBasket: () -> Unit,
    onDiscard: () -> Unit,
    setBarcode: (String) -> Unit,
    state: ScanState,
    filter: String,
    filteredItems: List<ShoppingItem>,
    setFilter: (String) -> Unit
) {
    Scaffold(topBar = {
        DefaultTopAppBar(
            onOpenSettings = openSettings,
            onOpenBenchmark = openBenchmark,
            onOpenAttacker = openAttacker
        )
    }) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            CameraPermission {
                Scanner(setBarcode)
            }
            SearchableItemList(filteredItems, setBarcode, filter, setFilter)

            AnimatedVisibility(
                visible = state != ScanEmptyState && state != ScanBlockedState,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f)
                        .background(Color.Black)
                )
            }
            when (state) {
                is ScanResultState -> ScannedItemCard(
                    state,
                    onAmountChange,
                    onAddToBasket,
                    onDiscard,
                    Modifier.align(Alignment.BottomCenter),
                )
                is ScanLoadingState -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableItemList(
    filteredItems: List<ShoppingItem>,
    setBarcode: (String) -> Unit,
    filter: String,
    setFilter: (String) -> Unit
) {
    val showSuggestions = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Column {
            if (showSuggestions.value && isKeyboardOpen == Keyboard.Opened) {
                Surface(shape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .padding(horizontal = 16.dp),
                    ) {
                        filteredItems.forEachIndexed { index, item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = {
                                    focusManager.clearFocus()
                                    setBarcode(item.id)
                                }) {
                                Text(item.title)
                            }

                            if (index < filteredItems.lastIndex) {
                                Divider()
                            }
                        }
                    }
                }
            }
            Surface() {
                OutlinedTextField(
                    value = filter,
                    onValueChange = setFilter,
                    label = { Text("Search") },
                    placeholder = { Text("Product Name") },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            showSuggestions.value = it.hasFocus
                        }
                        .onKeyEvent {
                            if (it.nativeKeyEvent.isCanceled) {
                                Timber.i("is cancelled")
                            }
                            true
                        }
                )
            }
        }
    }
}

@Composable
private fun ScannedItemCard(
    state: ScanResultState,
    setCount: (Int) -> Unit,
    add: () -> Unit,
    discard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = state.shoppingItem.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.priceSingle,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: ${state.priceTotal}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    NumberPicker(context).apply {
                        setOnValueChangedListener { _, _, count ->
                            setCount(count)
                        }
                        wrapSelectorWheel = false
                        minValue = 1
                        maxValue = 9999
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onClick = { discard() }
                ) {
                    Text(text = "Discard")
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onClick = { add() }
                ) {
                    Text(text = "Add")
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraPermission(content: @Composable (() -> Unit)) {

    // Permission (see https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/permissions/RequestPermissionSample.kt)
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        content()
    } else {
        LaunchedEffect(false) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}

@Composable
private fun Scanner(onScanBarcode: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = CameraPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Setup barcode analyzer
                val imageAnalysis = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(
                            executor,
                            BarcodeAnalyzer { barcode ->
                                onScanBarcode(barcode)
                            }
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize(),
    )
}

// https://stackoverflow.com/questions/68847559/how-can-i-detect-keyboard-opening-and-closing-in-jetpack-compose/69533584#69533584
enum class Keyboard {
    Opened, Closed
}

@Composable
fun keyboardAsState(): State<Keyboard> {
    val keyboardState = remember { mutableStateOf(Keyboard.Closed) }
    val view = LocalView.current
    DisposableEffect(view) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = if (keypadHeight > screenHeight * 0.15) {
                Keyboard.Opened
            } else {
                Keyboard.Closed
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }

    return keyboardState
}

@Composable
fun ScannedItemPreview() {
    CryptimeleonTheme {
        ScannedItemCard(
            state = ScanResultState(ShoppingItem("ADJFKLJLKSD", 999, "Apple"), 3),
            setCount = {},
            add = {},
            discard = {}
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    name = "Scanned Item in Light Mode"
)
fun ScannedItemPreviewLight() {
    ScannedItemPreview()
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Scanned Item in Dark Mode"
)
fun ScannedItemPreviewDark() {
    ScannedItemPreview()
}
