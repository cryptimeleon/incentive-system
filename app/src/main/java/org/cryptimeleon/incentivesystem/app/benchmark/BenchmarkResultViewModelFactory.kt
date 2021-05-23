package org.cryptimeleon.incentivesystem.app.benchmark

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class BenchmarkResultViewModelFactory(
    private val application: Application,
    private val joinRequest: LongArray,
    private val joinResponse: LongArray,
    private val joinHandleResponse: LongArray,
    private val earnRequest: LongArray,
    private val earnResponse: LongArray,
    private val earnHandleResponse: LongArray,
    private val spendRequest: LongArray,
    private val spendResponse: LongArray,
    private val spendHandleResponse: LongArray,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BenchmarkResultViewModel::class.java)) {
            return BenchmarkResultViewModel(
                application,
                joinRequest,
                joinResponse,
                joinHandleResponse,
                earnRequest,
                earnResponse,
                earnHandleResponse,
                spendRequest,
                spendResponse,
                spendHandleResponse
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}