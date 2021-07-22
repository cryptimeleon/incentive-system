package org.cryptimeleon.incentive.app.scan

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.databinding.FragmentScanResultBinding
import org.cryptimeleon.incentive.app.network.Item


/**
 * Fragment holding the result of the scan.
 * A user can choose a number of items, see the total price and add it to the basket.
 * Is displayed as a dialog that is opened at the bottom of the screen.
 */
class ScanResultFragment(private val scanResultFragmentCallback: ScanResultFragmentCallback) :
    BottomSheetDialogFragment() {

    private lateinit var binding: FragmentScanResultBinding
    private lateinit var viewModel: ScanResultViewModel
    private lateinit var item: Item

    /**
     * Setup data binding and the viewmodel when the view is created.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_scan_result,
            container,
            false
        )

        // Get item that is delivered via a parcel
        item = arguments?.getParcelable("item")!!

        viewModel = ViewModelProvider(
            this,
            ScanResultViewModelFactory(item)
        ).get(ScanResultViewModel::class.java)


        // Observe this variable of the view model and close the dialog when it becomes true
        viewModel.closeScanResult.observe(viewLifecycleOwner) {
            if (it == true) {
                dismiss()
                viewModel.closeScanResultFinished()
            }
        }

        binding.scanViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Setup number picker
        binding.amountPicker.wrapSelectorWheel = false
        binding.amountPicker.setOnValueChangedListener { _, _, it ->
            viewModel.onAmountChange(
                it
            )
        }

        return binding.root
    }

    /**
     * Override onDismiss to pass information to the listener.
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        scanResultFragmentCallback.scanResultFragmentDismissed()
    }

    /**
     * Override onCancel to pass information to the listener.
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        scanResultFragmentCallback.scanResultFragmentCanceled()
    }
}