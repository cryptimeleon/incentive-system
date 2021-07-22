package org.cryptimeleon.incentive.app.benchmark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkResult

/**
 * View Model of the Benchmark Result
 */
class BenchmarkResultViewModel(
    application: Application,
    benchmarkResult: BenchmarkResult
) : AndroidViewModel(application) {

    val joinText = protocolText(
        benchmarkResult.joinTotalAvg,
        benchmarkResult.joinRequestAvg,
        benchmarkResult.joinResponseAvg,
        benchmarkResult.joinHandleResponseAvg
    )
    val earnText = protocolText(
        benchmarkResult.earnTotalAvg,
        benchmarkResult.earnRequestAvg,
        benchmarkResult.earnResponseAvg,
        benchmarkResult.earnHandleResponseAvg
    )
    val spendText = protocolText(
        benchmarkResult.spendTotalAvg,
        benchmarkResult.spendRequestAvg,
        benchmarkResult.spendResponseAvg,
        benchmarkResult.spendHandleResponseAvg
    )
    val totalText = benchmarkResult.totalAvg.toString()

    private val _shareEvent = MutableLiveData(false)
    val shareEvent: LiveData<Boolean>
        get() = _shareEvent

    /**
     * Function for assembling the result string for each protocol
     */
    private fun protocolText(
        total: Double,
        request: Double,
        response: Double,
        handleResponse: Double
    ): String {
        return "Total: ${total.format(2)}\nRequest: ${request.format(2)}\nResponse: ${
            response.format(2)
        }\nHandle Response: ${handleResponse.format(2)}"
    }

    /**
     * Extension function for double to allow n decimal formatting
     */
    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

    /**
     * Assemble String that is sent via the share button.
     */
    fun computeShareData(): String {
        return "Total:\n$totalText\nIssueJoin:\n$joinText\nCreditEarn\n$earnText\nSpendDeduct\n$spendText"
    }

    fun shareFinished() {
        _shareEvent.value = false
    }

    fun startShare() {
        _shareEvent.value = true
    }
}