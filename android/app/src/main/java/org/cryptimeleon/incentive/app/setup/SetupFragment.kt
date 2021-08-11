package org.cryptimeleon.incentive.app.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.cryptimeleon.incentive.app.BaseFragment
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.dashboard.DashboardViewModel
import org.cryptimeleon.incentive.app.databinding.SetupFragmentBinding

@AndroidEntryPoint
class SetupFragment : BaseFragment() {

    override var bottomNavigationViewVisibility = View.INVISIBLE
    private val viewModel by viewModels<SetupViewModel>()
    private lateinit var binding: SetupFragmentBinding
    private val dashboardViewModel by activityViewModels<DashboardViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.setup_fragment,
            container,
            false
        )
        binding.setupViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Hide action bar
        (activity as AppCompatActivity).supportActionBar?.hide()

        viewModel.navigateToInfo.observe(viewLifecycleOwner, {
            if (it == true) {
                (activity as AppCompatActivity).supportActionBar?.show()
                dashboardViewModel.setupFinished.value = true
                findNavController().popBackStack()
            }
        })

        viewModel.exceptionToast.observe(viewLifecycleOwner) {
            if (it != "") {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.toastShown()
            }
        }
        return binding.root
    }
}