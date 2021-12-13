package org.cryptimeleon.incentive.app.ui.benchmark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.Setup
import org.cryptimeleon.incentive.crypto.benchmark.Benchmark
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkConfig
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkResult
import timber.log.Timber
import javax.inject.Inject

private const val BENCHMARK_ITERATIONS = 10
private val BENCHMARK_GROUP = Setup.BilinearGroupChoice.Herumi_MCL
const val SECURITY_PARAMETER = 128

enum class BenchmarkViewState {
    NOT_STARTED,
    SETUP,
    ISSUE_JOIN,
    CREDIT_EARN,
    SPEND_DEDUCT,
    FINISHED
}

/**
 * ViewModel for Benchmark, runs Benchmark in a Coroutine and triggers navigation to BenchmarkResultFragment when finished
 */
@HiltViewModel
class BenchmarkViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    val state = MutableLiveData<BenchmarkState>(
        BenchmarkState(
            BenchmarkViewState.NOT_STARTED,
            0,
            null
        )
    )

    /**
     * Runs the benchmark in a Coroutine
     */
    fun runBenchmark() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                Timber.i("Generating public parameters")
                state.postValue(
                    state.value!!.copy(
                        state = BenchmarkViewState.SETUP
                    )
                )

                val pp =
                    Setup.trustedSetup(SECURITY_PARAMETER, BENCHMARK_GROUP)

                val incentiveSystem = IncentiveSystem(pp)

                Timber.i("Provider Provider keys")
                val providerKeyPair = incentiveSystem.generateProviderKeys()

                Timber.i("Generating User keys")
                val userKeyPair = incentiveSystem.generateUserKeys()

                Timber.i("Generation finished")

                val benchmarkConfig = BenchmarkConfig(
                    BENCHMARK_ITERATIONS,
                    incentiveSystem,
                    pp,
                    providerKeyPair.pk,
                    providerKeyPair.sk,
                    userKeyPair.pk,
                    userKeyPair.sk
                )

                // Stop at this point if cancelled
                yield()

                // Run benchmark and use Consumer for ui feedback
                val benchmarkResult =
                    Benchmark.runBenchmark(benchmarkConfig) { benchmarkState, iteration ->
                        when (benchmarkState) {
                            org.cryptimeleon.incentive.crypto.benchmark.BenchmarkState.ISSUE_JOIN ->
                                state.postValue(
                                    state.value!!.copy(
                                        state = BenchmarkViewState.ISSUE_JOIN,
                                        iteration = iteration
                                    )
                                )
                            org.cryptimeleon.incentive.crypto.benchmark.BenchmarkState.CREDIT_EARN ->
                                state.postValue(
                                    state.value!!.copy(
                                        state = BenchmarkViewState.CREDIT_EARN,
                                        iteration = iteration
                                    )
                                )
                            org.cryptimeleon.incentive.crypto.benchmark.BenchmarkState.SPEND_DEDUCT ->
                                state.postValue(
                                    state.value!!.copy(
                                        state = BenchmarkViewState.SPEND_DEDUCT,
                                        iteration = iteration
                                    )
                                )
                        }
                    }

                // Stop at this point if cancelled
                yield()

                // Log the result arrays for debugging
                Timber.i(benchmarkResult.joinRequestTime.toString())
                Timber.i(benchmarkResult.joinResponseTime.toString())
                Timber.i(benchmarkResult.joinHandleResponseTime.toString())

                Timber.i(benchmarkResult.earnRequestTime.toString())
                Timber.i(benchmarkResult.earnResponseTime.toString())
                Timber.i(benchmarkResult.earnHandleResponseTime.toString())

                Timber.i(benchmarkResult.spendRequestTime.toString())
                Timber.i(benchmarkResult.spendResponseTime.toString())
                Timber.i(benchmarkResult.spendHandleResponseTime.toString())

                // Log the results
                benchmarkResult.printReport()

                // This triggers the navigation
                state.postValue(
                    state.value!!.copy(
                        state = BenchmarkViewState.FINISHED,
                        benchmarkResult = benchmarkResult
                    )
                )
            }
        }
    }
}

data class BenchmarkState(
    val state: BenchmarkViewState,
    val iteration: Int,
    val benchmarkResult: BenchmarkResult?
) {
    val stateText = when (state) {
        BenchmarkViewState.FINISHED -> "Done"
        BenchmarkViewState.SETUP -> "Setup of System"
        BenchmarkViewState.ISSUE_JOIN -> "Running issue-join ($iteration of $BENCHMARK_ITERATIONS)"
        BenchmarkViewState.CREDIT_EARN -> "Running credit-earn ($iteration of $BENCHMARK_ITERATIONS)"
        BenchmarkViewState.SPEND_DEDUCT -> "Running spend-deduct ($iteration of $BENCHMARK_ITERATIONS)"
        else -> "Other state"
    }

    val joinText = benchmarkResult?.let {
        protocolText(
            it.joinTotalAvg,
            it.joinRequestAvg,
            it.joinResponseAvg,
            it.joinHandleResponseAvg
        )
    }
    val earnText = benchmarkResult?.let {
        protocolText(
            it.earnTotalAvg,
            it.earnRequestAvg,
            it.earnResponseAvg,
            it.earnHandleResponseAvg
        )
    }
    val spendText = benchmarkResult?.let {
        protocolText(
            it.spendTotalAvg,
            it.spendRequestAvg,
            it.spendResponseAvg,
            it.spendHandleResponseAvg
        )
    }
    val totalText = benchmarkResult?.let { benchmarkResult.totalAvg.toString() }

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
    fun shareData(): String {
        return "Total:\n$totalText\nIssueJoin:\n$joinText\nCreditEarn\n$earnText\nSpendDeduct\n$spendText"
    }
}
