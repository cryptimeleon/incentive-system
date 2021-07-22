package org.cryptimeleon.incentive.app.benchmark

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.Setup
import org.cryptimeleon.incentive.crypto.benchmark.Benchmark
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkConfig
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkResult
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkState
import timber.log.Timber

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
class BenchmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val _benchmarkRunning = MutableLiveData(false)
    val benchmarkRunning: LiveData<Boolean>
        get() = _benchmarkRunning

    private val _currentState = MutableLiveData(BenchmarkViewState.NOT_STARTED)
    private val _iteration = MutableLiveData(0)

    private val _progressText = MediatorLiveData<String>()
    val progressText: LiveData<String>
        get() = _progressText

    private val _navigateToResults = MutableLiveData(false)
    val navigateToResults = _navigateToResults

    private val _usedGroupName = MutableLiveData<String>()
    val usedGroupName: LiveData<String>
        get() = _usedGroupName

    lateinit var benchmarkResult: BenchmarkResult

    init {
        // progressText is updated by two LiveData objects
        _progressText.addSource(_currentState) {
            _progressText.value = computeProgressText(it, _iteration.value!!)
        }
        _progressText.addSource(_iteration) {
            _progressText.value = computeProgressText(_currentState.value!!, it)
        }
        _usedGroupName.value = BENCHMARK_GROUP.name
    }

    /**
     * Maps a benchmarkState to a text that is shown to the user
     */
    private fun computeProgressText(state: BenchmarkViewState, iteration: Int): String {
        return when (state) {
            BenchmarkViewState.FINISHED -> "Done"
            BenchmarkViewState.SETUP -> "Setup of System"
            BenchmarkViewState.ISSUE_JOIN -> "Running issue-join (${iteration} of $BENCHMARK_ITERATIONS)"
            BenchmarkViewState.CREDIT_EARN -> "Running credit-earn (${iteration} of $BENCHMARK_ITERATIONS)"
            BenchmarkViewState.SPEND_DEDUCT -> "Running spend-deduct (${iteration} of $BENCHMARK_ITERATIONS)"
            else -> "Other state"
        }
    }


    /**
     * Runs the benchmark in a Coroutine
     */
    fun runBenchmark() {
        _benchmarkRunning.value = true

        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                Timber.i("Generating public parameters")
                _currentState.postValue(BenchmarkViewState.SETUP)

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
                benchmarkResult = Benchmark.runBenchmark(benchmarkConfig) { state, iteration ->
                    when (state) {
                        BenchmarkState.ISSUE_JOIN -> _currentState.postValue(
                            BenchmarkViewState.ISSUE_JOIN
                        )
                        BenchmarkState.CREDIT_EARN -> _currentState.postValue(
                            BenchmarkViewState.CREDIT_EARN
                        )
                        BenchmarkState.SPEND_DEDUCT -> _currentState.postValue(
                            BenchmarkViewState.SPEND_DEDUCT
                        )
                    }

                    _iteration.postValue(iteration)
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
                _currentState.postValue(BenchmarkViewState.FINISHED)
                _benchmarkRunning.postValue(false)
                _navigateToResults.postValue(true)
            }
        }
    }

    fun navigationFinished() {
        _navigateToResults.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("BenchmarkViewModel cleared")
    }
}