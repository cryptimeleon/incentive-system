package org.cryptimeleon.incentive.app.scan

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint

/**
 * This fragment hosts the page for scanning items and adding them to the basket.
 * When an item is scanned, it opens a dialog containing information about the item and allows
 * choosing an amount and adding it to the basket. For backwards communication, it implements the
 * ScanResultFragmentCallback.
 *
 * Based on:
 * https://developer.android.com/codelabs/camerax-getting-started#3
 * https://proandroiddev.com/create-vision-app-using-ml-kit-library-and-camerax-7bf022105604
 */
@AndroidEntryPoint
class ScanFragment : Fragment() {

    private val viewModel by viewModels<ScanViewModel>()


    /**
     * Setup view model and data binding on creating of the view.
     */
    @ExperimentalAnimationApi
    @ExperimentalPermissionsApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        // Dispose the Composition when viewLifecycleOwner is destroyed
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
        )

        setContent {
            MdcTheme {
                ScanScreen(viewModel)
            }
        }
    }


    /**
     * Simple image Analyzer for detecting barcodes.
     */
    class BarcodeAnalyzer(private val barcodeListener: BarcodeListener) : ImageAnalysis.Analyzer {
        // Get an instance of BarcodeScanner
        private val scanner = BarcodeScanning.getClient()

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                // Pass image to the scanner and have it do its thing
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        // Task completed successfully
                        for (barcode in barcodes) {
                            barcodeListener(barcode.rawValue ?: "")
                        }
                    }
                    .addOnFailureListener {
                        // Not sure if we need to do something in case of a failure, since we're scanning continuously.
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }
}

typealias BarcodeListener = (barcode: String) -> Unit