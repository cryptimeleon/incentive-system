package org.cryptimeleon.incentive.app.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.cryptimeleon.incentive.app.BaseFragment
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.databinding.DashboardFragmentBinding

class DashboardFragment : BaseFragment() {
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    override var bottomNavigationViewVisibility = View.VISIBLE
    lateinit var viewModel: DashboardViewModel
    lateinit var binding: DashboardFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        dashboardViewModel.setupFinished.observe(viewLifecycleOwner, {
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

        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
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