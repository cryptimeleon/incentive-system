package org.cryptimeleon.incentive.app.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import org.cryptimeleon.incentive.app.BaseFragment
import org.cryptimeleon.incentive.app.R

@AndroidEntryPoint
class DashboardFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.VISIBLE

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

        return ComposeView(requireContext()).apply {
            // Dispose the Composition when viewLifecycleOwner is destroyed
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    Dashboard(viewModel)
                }
            }
        }
    }
}