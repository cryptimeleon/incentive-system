package org.cryptimeleon.incentivesystem.app.benchmark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

class BenchmarkResultViewModel(
    application: Application,
    private val tJoinRequest: LongArray,
    private val tJoinResponse: LongArray,
    private val tJoinHandleResponse: LongArray,
    private val tEarnRequest: LongArray,
    private val tEarnResponse: LongArray,
    private val tEarnHandleResponse: LongArray,
    private val tSpendRequest: LongArray,
    private val tSpendResponse: LongArray,
    private val tSpendHandleResponse: LongArray
) : AndroidViewModel(application) {

    private val tJoin = add(tJoinRequest, tJoinResponse, tJoinHandleResponse)
    private val tEarn = add(tEarnRequest, tEarnResponse, tEarnHandleResponse)
    private val tSpend = add(tSpendRequest, tSpendResponse, tSpendHandleResponse)
    private val tTotal = add(tJoin, tEarn, tSpend)

    val joinText = protocolText(tJoin, tJoinRequest, tJoinRequest, tJoinHandleResponse)
    val earnText = protocolText(tEarn, tEarnRequest, tEarnRequest, tEarnHandleResponse)
    val spendText = protocolText(tSpend, tSpendRequest, tSpendRequest, tSpendHandleResponse)
    val totalText = analyzeData(tTotal)

    private val _shareEvent = MutableLiveData(false)
    val shareEvent: LiveData<Boolean>
        get() = _shareEvent

    private fun protocolText(
        total: LongArray,
        request: LongArray,
        response: LongArray,
        handleResponse: LongArray
    ): String {
        return "Total: ${analyzeData(total)}\nRequest: ${analyzeData(request)}\nResponse: ${
            analyzeData(response)
        }\nHandle Response: ${analyzeData(handleResponse)}"
    }

    private fun add(vararg arrays: LongArray): LongArray {
        val result = LongArray(arrays[0].size)
        for (i in result.indices) {
            result[i] = arrays.map { longs: LongArray -> longs[i] }.reduce { a, b -> a + b }
        }
        return result
    }

    private fun analyzeData(data: LongArray): String {
        val stats = DescriptiveStatistics()

        for (x in data) {
            stats.addValue(
                x / 10000000.0
            )
        }

        val mean = stats.mean
        return "${mean.format(2)}ms"
    }

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

    fun shareFinished() {
        _shareEvent.value = false
    }

    fun startShare() {
        _shareEvent.value = true
    }

    fun computeShareData(): String {
        val gson = Gson()
        val resultJson = ResultJson(
            tTotal,
            tJoin,
            tJoinRequest,
            tJoinResponse,
            tJoinHandleResponse,
            tEarn,
            tEarnRequest,
            tEarnResponse,
            tEarnHandleResponse,
            tSpend,
            tSpendRequest,
            tSpendResponse,
            tSpendHandleResponse
        )
        return gson.toJson(resultJson)
    }

    /*
     * Simple data class for JSON serialization of the raw data
     */
    data class ResultJson(
        val totalTime: LongArray,
        val joinTime: LongArray,
        val joinRequestTime: LongArray,
        val joinResponseTime: LongArray,
        val joinHandleResponseTime: LongArray,
        val earnTime: LongArray,
        val earnRequestTime: LongArray,
        val earnResponseTime: LongArray,
        val earnHandleResponeTime: LongArray,
        val spendTime: LongArray,
        val spendRequestTime: LongArray,
        val spendResponseTime: LongArray,
        val spendHandleResponseTime: LongArray
    )
}