package org.cryptimeleon.incentivesystem.app.benchmark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

class BenchmarkResultViewModel(
    application: Application,
    val tJoinRequest: LongArray,
    val tJoinResponse: LongArray,
    val tJoinHandleResponse: LongArray,
    val tEarnRequest: LongArray,
    val tEarnResponse: LongArray,
    val tEarnHandleResponse: LongArray,
    val tSpendRequest: LongArray,
    val tSpendResponse: LongArray,
    val tSpendHandleResponse: LongArray
) : AndroidViewModel(application) {

    val tJoin = add(tJoinRequest, tJoinResponse, tJoinHandleResponse)
    val tEarn = add(tEarnRequest, tEarnResponse, tEarnHandleResponse)
    val tSpend = add(tSpendRequest, tSpendResponse, tSpendHandleResponse)
    val tTotal = add(tJoin, tEarn, tSpend)

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

    fun add(vararg arrays: LongArray): LongArray {
        val result = LongArray(arrays[0].size)
        for (i in result.indices) {
            result[i] = arrays.map { longs: LongArray -> longs[i] }.reduce { a, b -> a + b }
        }
        return result
    }

    fun analyzeData(data: LongArray): String {
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
            tJoinRequest,
            tJoinResponse,
            tJoinHandleResponse,
            tEarnRequest,
            tEarnResponse,
            tEarnHandleResponse,
            tSpendRequest,
            tSpendResponse,
            tSpendHandleResponse
        )
        return gson.toJson(resultJson)
    }

    data class ResultJson(
        val tJoinRequest: LongArray,
        val tJoinResponse: LongArray,
        val tJoinHandleResponse: LongArray,
        val tEarnRequest: LongArray,
        val tEarnResponse: LongArray,
        val tEarnHandleResponse: LongArray,
        val tSpendRequest: LongArray,
        val tSpendResponse: LongArray,
        val tSpendHandleResponse: LongArray
    )
}