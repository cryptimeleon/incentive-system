package org.cryptimeleon.incentivesystem.app.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import org.cryptimeleon.incentivesystem.app.R
import org.cryptimeleon.incentivesystem.app.databinding.InfoFragmentBinding
import org.cryptimeleon.incentivesystem.app.setup.SetupFragmentDirections
import org.cryptimeleon.incentivesystem.app.setup.SetupViewModel

class InfoFragment : Fragment() {

    lateinit var viewModel: InfoViewModel
    lateinit var binding: InfoFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this).get(InfoViewModel::class.java)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.info_fragment,
            container,
            false
        )
        binding.infoViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.exceptionToast.observe(viewLifecycleOwner) {
            if (it != "") {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.toastShown()
            }
        }

        return binding.root
    }

}