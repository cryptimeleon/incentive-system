package org.cryptimeleon.incentivesystem.app.benchmark

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature
import org.cryptimeleon.incentivesystem.app.repository.BenchmarkRepository
import org.cryptimeleon.incentivesystem.app.setup.SECURITY_PARAMETER
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem
import org.cryptimeleon.incentivesystem.cryptoprotocol.Setup
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.*
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinResponse
import timber.log.Timber
import java.math.BigInteger
import kotlin.system.measureNanoTime

private const val BENCHMARK_ITERATIONS = 100
private val BENCHMARK_GROUP = Setup.BilinearGroupChoice.Herumi_MCL
private val EARN_SPEND_AMOUNT = BigInteger.valueOf(1000)

enum class BenchmarkState {
    NOT_STARTED,
    SETUP,
    ISSUE_JOIN,
    CREDIT_EARN,
    SPEND_DEDUCT,
    FINISHED
}

class BenchmarkViewModel(application: Application) : AndroidViewModel(application) {

    var tJoinRequest = LongArray(BENCHMARK_ITERATIONS)
    var tJoinResponse = LongArray(BENCHMARK_ITERATIONS)
    var tJoinHandleResponse = LongArray(BENCHMARK_ITERATIONS)
    var tEarnRequest = LongArray(BENCHMARK_ITERATIONS)
    var tEarnResponse = LongArray(BENCHMARK_ITERATIONS)
    var tEarnHandleResponse = LongArray(BENCHMARK_ITERATIONS)
    var tSpendRequest = LongArray(BENCHMARK_ITERATIONS)
    var tSpendResponse = LongArray(BENCHMARK_ITERATIONS)
    var tSpendHandleResponse = LongArray(BENCHMARK_ITERATIONS)

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _benchmarkRunning = MutableLiveData(false)
    val benchmarkRunning: LiveData<Boolean>
        get() = _benchmarkRunning

    private val _currentState = MutableLiveData(BenchmarkState.NOT_STARTED)
    private val _iteration = MutableLiveData(0)

    private val _progressText = MediatorLiveData<String>()
    val progressText: LiveData<String>
        get() = _progressText

    private val _navigateToResults = MutableLiveData(false)
    val navigateToResults = _navigateToResults

    private val _usedGroupName = MutableLiveData<String>()
    val usedGroupName: LiveData<String>
        get() = _usedGroupName

    init {
        _progressText.addSource(_currentState) {
            _progressText.value = computeProgressText(it, _iteration.value!!)
        }
        _progressText.addSource(_iteration) {
            _progressText.value = computeProgressText(_currentState.value!!, it)
        }
        _usedGroupName.value = BENCHMARK_GROUP.name
    }

    private fun computeProgressText(state: BenchmarkState, iteration: Int): String {
        return when (state) {
            BenchmarkState.FINISHED -> "Done"
            BenchmarkState.SETUP -> "Setup of System"
            BenchmarkState.ISSUE_JOIN -> "Running issue-join (${iteration} of $BENCHMARK_ITERATIONS)"
            BenchmarkState.CREDIT_EARN -> "Running credit-earn (${iteration} of $BENCHMARK_ITERATIONS)"
            BenchmarkState.SPEND_DEDUCT -> "Running spend-deduct (${iteration} of $BENCHMARK_ITERATIONS)"
            else -> "Other state"
        }
    }


    fun startBenchmark() {
        _benchmarkRunning.value = true

        uiScope.launch {
            withContext(Dispatchers.Default) {
                Timber.i("Generating public parameters")
                _currentState.postValue(BenchmarkState.SETUP)

                val pp =
                    Setup.trustedSetup(SECURITY_PARAMETER, BENCHMARK_GROUP)

                val incentiveSystem = IncentiveSystem(pp)

                Timber.i("Provider Provider keys")
                val providerKeyPair = incentiveSystem.generateProviderKeys()

                Timber.i("Generating User keys")
                val userKeyPair = incentiveSystem.generateUserKeys()

                Timber.i("Generation finished")

                val ppk = providerKeyPair.pk
                val psk = providerKeyPair.sk
                val upk = userKeyPair.pk
                val usk = userKeyPair.sk
                lateinit var token: Token

                var joinRequest: JoinRequest
                var joinResponse: JoinResponse
                var earnRequest: EarnRequest
                var earnResponse: SPSEQSignature
                var spendRequest: SpendRequest
                var spendResponseTuple: SpendProviderOutput

                _currentState.postValue(BenchmarkState.ISSUE_JOIN)
                for (i in 1..BENCHMARK_ITERATIONS) {
                    _iteration.postValue(i)

                    tJoinRequest[i - 1] = measureNanoTime {
                        joinRequest = incentiveSystem.generateJoinRequest(
                            pp,
                            ppk,
                            UserKeyPair(upk, usk)
                        )
                    }
                    tJoinResponse[i - 1] = measureNanoTime {
                        joinResponse =
                            incentiveSystem.generateJoinRequestResponse(
                                pp,
                                ProviderKeyPair(psk, ppk),
                                upk.upk,
                                joinRequest
                            )
                    }
                    tJoinHandleResponse[i - 1] = measureNanoTime {
                        token = incentiveSystem.handleJoinRequestResponse(
                            pp,
                            ppk,
                            UserKeyPair(upk, usk),
                            joinRequest,
                            joinResponse
                        )
                    }
                }

                _currentState.postValue(BenchmarkState.CREDIT_EARN)

                for (i in 1..BENCHMARK_ITERATIONS) {
                    _iteration.postValue(i)
                    tEarnRequest[i - 1] = measureNanoTime {
                        earnRequest = incentiveSystem.generateEarnRequest(
                            token,
                            ppk,
                            UserKeyPair(upk, usk)
                        )
                    }
                    tEarnResponse[i - 1] = measureNanoTime {
                        earnResponse = incentiveSystem.generateEarnRequestResponse(
                            earnRequest,
                            EARN_SPEND_AMOUNT,
                            ProviderKeyPair(psk, ppk)
                        )
                    }
                    tEarnHandleResponse[i - 1] = measureNanoTime {
                        token = incentiveSystem.handleEarnRequestResponse(
                            earnRequest,
                            earnResponse,
                            EARN_SPEND_AMOUNT,
                            token,
                            ppk,
                            UserKeyPair(upk, usk)
                        )
                    }
                }

                _currentState.postValue(BenchmarkState.SPEND_DEDUCT)
                for (i in 1..BENCHMARK_ITERATIONS) {
                    _iteration.postValue(i)
                    val tid = incentiveSystem.pp.bg.zn.uniformlyRandomElement
                    tSpendRequest[i - 1] = measureNanoTime {
                        spendRequest = incentiveSystem.generateSpendRequest(
                            token,
                            ppk,
                            EARN_SPEND_AMOUNT,
                            UserKeyPair(upk, usk),
                            tid
                        )
                    }

                    tSpendResponse[i - 1] = measureNanoTime {
                        spendResponseTuple = incentiveSystem.generateSpendRequestResponse(
                            spendRequest,
                            ProviderKeyPair(psk, ppk),
                            EARN_SPEND_AMOUNT,
                            tid
                        )
                    }

                    tSpendHandleResponse[i - 1] = measureNanoTime {
                        token = incentiveSystem.handleSpendRequestResponse(
                            spendResponseTuple.spendResponse,
                            spendRequest,
                            token,
                            EARN_SPEND_AMOUNT,
                            ppk,
                            UserKeyPair(upk, usk)
                        )
                    }
                }

                // This triggers the navigation
                _currentState.postValue(BenchmarkState.FINISHED)
                Thread.sleep(200)
                _benchmarkRunning.postValue(false)
                _navigateToResults.postValue(true)
            }
        }
    }

    fun navigationFinished() {
        _navigateToResults.value = false
    }

    override fun onCleared() {
        viewModelJob.cancel()
        Timber.i("Benchmark canceled")
        super.onCleared()
    }
}