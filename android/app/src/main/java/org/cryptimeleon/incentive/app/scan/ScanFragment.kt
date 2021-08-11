package org.cryptimeleon.incentive.app.scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.databinding.FragmentScanBinding
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
class ScanFragment : Fragment(), ScanResultFragmentCallback {

    private lateinit var binding: FragmentScanBinding
    private val viewModel by viewModels<ScanViewModel>()
    private lateinit var cameraExecutor: ExecutorService


    /**
     * Setup view model and data binding on creating of the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_scan,
            container,
            false
        )

        // Observer show item to start the itemResultFragment when an item was successfully scanned
        viewModel.showItem.observe(viewLifecycleOwner) {
            if (it == true) {
                // Transfer item to the item fragment
                val bundle = bundleOf("item" to viewModel.item.value)
                val scanResultFragment = ScanResultFragment(this)
                scanResultFragment.arguments = bundle
                scanResultFragment.show(
                    (requireActivity() as AppCompatActivity).supportFragmentManager,
                    "scanResultFragment"
                )
            }
        }

        binding.scanViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Request camera permissions
        // TODO handle permission denial
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding.root
    }

    /**
     * Check whether all required permissions were granted.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Start the camera and setup an analyzer that uses the Google ML kit for detecting barcodes.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // show preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(
                    binding.previewView.surfaceProvider
                )
            }

            // Setup barcode analyzer
            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        viewModel.setBarcode(barcode)
                    })
                }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind any bound use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to lifecycleOwner
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Timber.e("Binding failed! :( $e")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Callback function for the result from the permission request dialog.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Turn off the camera when the fragment is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    /**
     * Function implementing the callback interface for the result fragment.
     * Is called when the result fragment was canceled.
     */
    override fun scanResultFragmentCanceled() {
        viewModel.showItemFinished()
    }

    /**
     * Function implementing the callback interface for the result fragment.
     * Is called when the result fragment was dismissed.
     */
    override fun scanResultFragmentDismissed() {
        viewModel.showItemFinished()
    }


    /**
     * Companion object with the required permissions.
     */
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
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