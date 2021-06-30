package org.cryptimeleon.incentive.app.benchmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.databinding.BenchmarkFragmentBinding

/**
 * UI Fragment of the Benchmark
 */
class BenchmarkFragment : Fragment() {
    private lateinit var viewModel: BenchmarkViewModel
    private lateinit var binding: BenchmarkFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.benchmark_fragment,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(BenchmarkViewModel::class.java)
        viewModel.navigateToResults.observe(viewLifecycleOwner) {
            if (it == true) {
                val action =
                    BenchmarkFragmentDirections.actionBenchmarkFragmentToBenchmarkResultFragment(
                        viewModel.benchmarkResult
                    )
                findNavController(this).navigate(action)
                viewModel.navigationFinished()
            }
        }

        binding.benchmarkViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}