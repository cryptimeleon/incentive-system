package org.cryptimeleon.incentivesystem.app.benchmark

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark.BenchmarkResult

/**
 * Factory required to create BenchmarkResultViewModel with parameters
 */
class BenchmarkResultViewModelFactory(
    private val application: Application,
    private val result: BenchmarkResult,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BenchmarkResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BenchmarkResultViewModel(
                application,
                result
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}