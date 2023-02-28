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
import org.cryptimeleon.incentive.crypto.BilinearGroupChoice
import org.cryptimeleon.incentive.crypto.benchmark.Benchmark
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkConfig
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkResult
import timber.log.Timber
import javax.inject.Inject

private const val BENCHMARK_ITERATIONS = 100
private val BENCHMARK_GROUP = BilinearGroupChoice.Herumi_MCL
const val SECURITY_PARAMETER = 128

enum class BenchmarkViewState {
    NOT_STARTED,
    SETUP,
    REGISTRATION,
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

    val state = MutableLiveData(
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
                Timber.i("Setup")
                state.postValue(
                    state.value!!.copy(
                        state = BenchmarkViewState.SETUP
                    )
                )

                val benchmarkConfig = BenchmarkConfig(
                    BENCHMARK_ITERATIONS,
                    SECURITY_PARAMETER,
                    BENCHMARK_GROUP
                )

                // Stop at this point if cancelled
                yield()

                // Run benchmark and use Consumer for ui feedback
                val benchmarkResult =
                    Benchmark.runBenchmark(benchmarkConfig) { benchmarkState, iteration ->
                        when (benchmarkState) {
                            null -> Timber.e("Benchmark State is null")
                            org.cryptimeleon.incentive.crypto.benchmark.BenchmarkState.REGISTRATION ->
                                state.postValue(
                                    state.value!!.copy(
                                        state = BenchmarkViewState.REGISTRATION,
                                        iteration = iteration
                                    )
                                )
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

                // Log the results
                benchmarkResult.printCSV()
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
        BenchmarkViewState.REGISTRATION-> "Running registration ($iteration of $BENCHMARK_ITERATIONS)"
        BenchmarkViewState.ISSUE_JOIN -> "Running issue-join ($iteration of $BENCHMARK_ITERATIONS)"
        BenchmarkViewState.CREDIT_EARN -> "Running credit-earn ($iteration of $BENCHMARK_ITERATIONS)"
        BenchmarkViewState.SPEND_DEDUCT -> "Running spend-deduct ($iteration of $BENCHMARK_ITERATIONS)"
        else -> "Other state"
    }

    val registrationText = benchmarkResult?.let {
        protocolText(
            it.registrationAppAvg,
            it.registrationStoreRequestAvg,
            it.registrationProviderRequestAvg,
            it.registrationHandleResponseAvg
        )
    }
    val joinText = benchmarkResult?.let {
        protocolText(
            it.joinAppAvg,
            it.joinStoreRequestAvg,
            it.joinProviderRequestAvg,
            it.joinHandleResponseAvg
        )
    }
    val earnText = benchmarkResult?.let {
        protocolText(
            it.earnAppAvg,
            it.earnStoreRequestAvg,
            it.earnProviderRequestAvg,
            it.earnHandleResponseAvg
        )
    }
    val spendText = benchmarkResult?.let {
        protocolText(
            it.spendAppAvg,
            it.spendStoreRequestAvg,
            it.spendProviderRequestAvg,
            it.spendHandleResponseAvg
        )
    }

    /**
     * Function for assembling the result string for each protocol
     */
    private fun protocolText(
        total: Double,
        storeRequest: Double,
        providerRequest: Double,
        handleResponse: Double
    ): String {
        return "Total: ${total.format(3)}ms\nStore Request: ${storeRequest.format(3)}ms\nProvider Request: ${
            providerRequest.format(3)
        }ms\nHandle Response: ${handleResponse.format(2)}ms"
    }

    /**
     * Extension function for double to allow n decimal formatting
     */
    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

    /**
     * Assemble String that is sent via the share button.
     */
    fun shareData(): String {
        return "Total:\nRegistration:\n$registrationText\nIssueJoin:\n$joinText\nCreditEarn\n$earnText\nSpendDeduct\n$spendText"
    }
}
