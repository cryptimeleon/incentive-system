package org.cryptimeleon.incentive.app.scan

import android.content.res.Configuration
import android.widget.NumberPicker
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import org.cryptimeleon.incentive.app.data.network.Item
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import androidx.camera.core.Preview as CameraPreview

@ExperimentalAnimationApi
@ExperimentalPermissionsApi
@Composable
fun ScanScreen() {
    val viewModel = hiltViewModel<ScanViewModel>()
    val state by viewModel.state.observeAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CameraPermission {
            Scanner(viewModel::setBarcode)
        }

        AnimatedVisibility(
            visible = state != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f)
                    .background(Color.Black)
            )
        }
        state?.let {
            ScannedItemCard(
                it,
                { amount -> viewModel.onAmountChange(amount) },
                { viewModel.onAddToBasket() },
                { viewModel.onDiscardItem() },
                Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun ScannedItemCard(
    state: ScanState,
    setCount: (Int) -> Unit,
    add: () -> Unit,
    discard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = state.item.title,
                style = MaterialTheme.typography.h5
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.priceSingle,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = "Total: ${state.priceTotal}",
                    style = MaterialTheme.typography.body1
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
                    onClick = { discard() }) {
                    Text(text = "Discard")
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onClick = { add() }) {
                    Text(text = "Add")
                }
            }
        }
    }
}


@ExperimentalPermissionsApi
@Composable
private fun CameraPermission(content: @Composable (() -> Unit)) {

    // Permission
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            LaunchedEffect(false) {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        permissionNotAvailableContent = {},
        content = content
    )
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
                        it.setAnalyzer(executor, BarcodeAnalyzer { barcode ->
                            onScanBarcode(barcode)
                        })
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

@Composable
fun ScannedItemPreview() {
    CryptimeleonTheme {
        ScannedItemCard(
            state = ScanState(Item("ADJFKLJLKSD", 999, "Apple"), 3),
            setCount = {},
            add = {},
            discard = {}
        )

    }
}

@Composable
@Preview(
    showBackground = true,
    name = "Scan Screen in Dark Mode"
)
fun ScannedItemPreviewLight() {
    ScannedItemPreview()
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Scan Screen in Dark Mode"
)
fun ScannedItemPreviewDark() {
    ScannedItemPreview()
}

@ExperimentalPermissionsApi
@Composable
fun ScanScreenPreview() {
    CryptimeleonTheme {
        Scanner {}
    }
}

@ExperimentalPermissionsApi
@Composable
@Preview(
    showBackground = true,
    name = "Scan Screen in Light Mode"
)
fun ScanScreenPreviewLight() {
    ScanScreenPreview()
}

@ExperimentalPermissionsApi
@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Scan Screen in Dark Mode"
)
fun ScanScreenPreviewDark() {
    ScanScreenPreview()
}
