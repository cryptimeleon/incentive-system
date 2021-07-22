package org.cryptimeleon.incentive.app.scan

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.cryptimeleon.incentive.app.network.Item

/**
 * Factory required to create BenchmarkResultViewModel with parameters.
 */
class ScanResultViewModelFactory(
    private val item: Item,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanResultViewModel(
                item,
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}