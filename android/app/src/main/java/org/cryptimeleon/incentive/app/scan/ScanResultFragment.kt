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


class ScanResultFragment(val scanResultFragmentCallback: ScanResultFragmentCallback) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentScanResultBinding
    private lateinit var viewModel: ScanResultViewModel
    private lateinit var item: Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_scan_result,
            container,
            false
        )

        item = arguments?.getParcelable("item")!!

        viewModel = ViewModelProvider(
            this,
            ScanResultViewModelFactory(item)
        ).get(ScanResultViewModel::class.java)


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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        scanResultFragmentCallback.scanResultFragmentDismissed()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        scanResultFragmentCallback.scanResultFragmentCanceled()
    }


}