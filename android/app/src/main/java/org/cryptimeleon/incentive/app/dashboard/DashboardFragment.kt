package org.cryptimeleon.incentive.app.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.cryptimeleon.incentive.app.BaseFragment
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.databinding.DashboardFragmentBinding

@AndroidEntryPoint
class DashboardFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.VISIBLE
    lateinit var binding: DashboardFragmentBinding

    // Shared view model through the activityViewModels mechanism
    private val viewModel by activityViewModels<DashboardViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        viewModel.setupFinished.observe(viewLifecycleOwner, {
            if (!it) {
                navController.navigate(R.id.setup)
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dashboard_fragment,
            container,
            false
        )
        binding.infoViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}