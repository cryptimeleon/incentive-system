package org.cryptimeleon.incentivesystem.app.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import org.cryptimeleon.incentivesystem.app.R
import org.cryptimeleon.incentivesystem.app.databinding.SetupFragmentBinding


class SetupFragment : Fragment() {

    private lateinit var viewModel: SetupViewModel
    private lateinit var binding: SetupFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SetupViewModel::class.java)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.setup_fragment,
            container,
            false
        )
        binding.setupViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.navigateToInfo.observe(viewLifecycleOwner, {
            if (it == true) {
                val action = SetupFragmentDirections.actionSetupToInfo()
                NavHostFragment.findNavController(this).navigate(action)
                viewModel.navigateToInfoFinished()
            }
        })
        return binding.root
    }
}