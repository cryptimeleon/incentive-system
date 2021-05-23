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
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderSecretKey
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinResponse
import org.cryptimeleon.math.serialization.converter.JSONConverter
import timber.log.Timber
import java.math.BigInteger
import kotlin.system.measureNanoTime

private const val BENCHMARK_ITERATIONS = 100
private val BENCHMARK_GROUP = Setup.BilinearGroupChoice.Debug
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

    private val benchmarkRepository = BenchmarkRepository(application.applicationContext)

    private val _benchmarkRunning = MutableLiveData(false)
    val benchmarkRunning: LiveData<Boolean>
        get() = _benchmarkRunning

    private val _currentState = MutableLiveData(BenchmarkState.NOT_STARTED)
    val currentState: LiveData<BenchmarkState>
        get() = _currentState

    private val _iteration = MutableLiveData(0)
    val iteration: LiveData<Int>
        get() = _iteration

    private val _progressText = MediatorLiveData<String>()
    val progressText: LiveData<String>
        get() = _progressText

    private val _navigateToResults = MutableLiveData(false)
    val navigateToResults = _navigateToResults

    init {
        _progressText.addSource(_currentState) {
            _progressText.value = computeProgressText(it, _iteration.value!!)
        }
        _progressText.addSource(_iteration) {
            _progressText.value = computeProgressText(_currentState.value!!, it)
        }
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
        if (_benchmarkRunning.value == true) {
            return
        }
        _benchmarkRunning.value = true

        uiScope.launch {
            withContext(Dispatchers.IO) {
                _currentState.postValue(BenchmarkState.SETUP)
                if (!benchmarkRepository.getSetupFinished()) {
                    setupBenchmark()
                }

                val jsonConverter = JSONConverter()
                val pp =
                    IncentivePublicParameters(jsonConverter.deserialize(benchmarkRepository.getPublicParameters()))
                val ppk = ProviderPublicKey(
                    jsonConverter.deserialize(benchmarkRepository.getProviderPublicKey()),
                    pp.spsEq,
                    pp.bg.g1
                )
                val psk = ProviderSecretKey(
                    jsonConverter.deserialize(benchmarkRepository.getProviderSecretKey()),
                    pp.spsEq,
                    pp.bg.zn,
                    pp.prfToZn
                )
                val upk = UserPublicKey(
                    jsonConverter.deserialize(benchmarkRepository.getUserPublicKey()),
                    pp.bg.g1
                )
                val usk = UserSecretKey(
                    jsonConverter.deserialize(benchmarkRepository.getUserSecretKey()),
                    pp.bg.zn,
                    pp.prfToZn
                )
                val incentiveSystem = IncentiveSystem(pp)
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


    private fun setupBenchmark() {
        Timber.i("Generating public parameters")
        val jsonConverter = JSONConverter()

        val incentivePublicParameters =
            Setup.trustedSetup(SECURITY_PARAMETER, BENCHMARK_GROUP)
        benchmarkRepository.setPublicParameters(
            jsonConverter.serialize(
                incentivePublicParameters.representation
            )
        )

        val incentiveSystem = IncentiveSystem(incentivePublicParameters)

        Timber.i("Provider Provider keys")
        val providerKeyPair = incentiveSystem.generateProviderKeys()
        benchmarkRepository.setProviderPublicKey(jsonConverter.serialize(providerKeyPair.pk.representation))
        benchmarkRepository.setProviderSecretKey(jsonConverter.serialize(providerKeyPair.sk.representation))

        Timber.i("Generating User keys")
        val userKeyPair = incentiveSystem.generateUserKeys()
        benchmarkRepository.setUserPublicKey(jsonConverter.serialize(userKeyPair.pk.representation))
        benchmarkRepository.setUserSecretKey(jsonConverter.serialize(userKeyPair.sk.representation))

        benchmarkRepository.setSetupFinished(true)
    }

    fun navigationFinished() {
        _navigateToResults.value = false
    }
}