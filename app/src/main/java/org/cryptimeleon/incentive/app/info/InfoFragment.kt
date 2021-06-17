package org.cryptimeleon.incentive.app.info

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.databinding.InfoFragmentBinding

class InfoFragment : Fragment() {

    lateinit var viewModel: InfoViewModel
    lateinit var binding: InfoFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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