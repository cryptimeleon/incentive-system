package org.cryptimeleon.incentivesystem.app.info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.*
import org.cryptimeleon.incentivesystem.app.repository.CryptoRepository
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderSecretKey
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey
import org.cryptimeleon.math.serialization.converter.JSONConverter
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.math.BigInteger

class InfoViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val cryptoRepository = CryptoRepository(application.applicationContext)
    private lateinit var incentiveSystem: IncentiveSystem

    private val _incentivePublicParameters = MutableLiveData<IncentivePublicParameters>()
    private val _providerPublicKey = MutableLiveData<ProviderPublicKey>()
    private val _providerSecretKey = MutableLiveData<ProviderSecretKey>()
    private val _userSecretKey = MutableLiveData<UserSecretKey>()
    private val _userPublicKey = MutableLiveData<UserPublicKey>()
    private val _token = MutableLiveData<Token>()

    val incentivePublicParametersText: LiveData<String> =
        Transformations.map(_incentivePublicParameters) {
            it?.let {
                it.toString()
            }
        }

    val providerPublicKey: LiveData<String> =
        Transformations.map(_providerPublicKey) {
            it?.let {
                it.toString()
            }
        }

    val providerSecretKey: LiveData<String> =
        Transformations.map(_providerSecretKey) {
            it?.let {
                it.toString()
            }
        }

    val userSecretKey: LiveData<String> =
        Transformations.map(_userSecretKey) {
            it?.let {
                it.toString()
            }
        }

    val userPublicKey: LiveData<String> =
        Transformations.map(_userPublicKey) {
            it?.let {
                it.toString()
            }
        }

    val token: LiveData<String> = Transformations.map(_token) {
        if (it == null) {
            "No token"
        } else {
            tokenRepresentation(it)
        }
    }

    private fun tokenRepresentation(token: Token): String {
        return "Points: ${token.points.integer}\n" +
                "Signature: ${token.signature}"
    }

    private val _updatingToken = MutableLiveData(true)
    val updatingToken: LiveData<Boolean>
        get() = _updatingToken

    private val _exceptionToast = MutableLiveData("")
    val exceptionToast: LiveData<String>
        get() = _exceptionToast

    init {
        loadCryptoAssets()
    }

    fun createToken() {
        uiScope.launch {
            _updatingToken.value = true
            withContext(Dispatchers.IO) {
                Timber.i("Run issue-join to generate a new token.")
                val userKeyPair = UserKeyPair(_userPublicKey.value, _userSecretKey.value)
                val providerKeyPair =
                    ProviderKeyPair(_providerSecretKey.value, _providerPublicKey.value)
                val joinRequest = incentiveSystem.generateJoinRequest(
                    _incentivePublicParameters.value,
                    _providerPublicKey.value,
                    userKeyPair
                )
                val joinResponse =
                    incentiveSystem.generateJoinRequestResponse(
                        _incentivePublicParameters.value,
                        providerKeyPair,
                        _userPublicKey.value!!.upk,
                        joinRequest
                    )
                val newToken = incentiveSystem.handleJoinRequestResponse(
                    _incentivePublicParameters.value,
                    _providerPublicKey.value,
                    userKeyPair,
                    joinRequest,
                    joinResponse
                )
                val jsonConverter = JSONConverter()
                newToken?.let {
                    cryptoRepository.setToken(jsonConverter.serialize(it.representation))
                }
                _token.postValue(newToken)
            }
            _updatingToken.value = false
        }
    }

    fun earn(amount: Int) {
        uiScope.launch {
            _updatingToken.value = true
            withContext(Dispatchers.IO) {
                Timber.i("Run credit-earn to update tokens value")
                val userKeyPair = UserKeyPair(_userPublicKey.value, _userSecretKey.value)
                val providerKeyPair =
                    ProviderKeyPair(_providerSecretKey.value, _providerPublicKey.value)
                val earnRequest = incentiveSystem.generateEarnRequest(
                    _token.value,
                    _providerPublicKey.value,
                    userKeyPair
                )
                val earnResponse = incentiveSystem.generateEarnRequestResponse(
                    earnRequest,
                    BigInteger.valueOf(amount.toLong()),
                    providerKeyPair
                )
                val newToken = incentiveSystem.handleEarnRequestResponse(
                    earnRequest,
                    earnResponse,
                    BigInteger.valueOf(amount.toLong()),
                    _token.value,
                    _providerPublicKey.value,
                    userKeyPair
                )
                val jsonConverter = JSONConverter()
                newToken?.let {
                    cryptoRepository.setToken(jsonConverter.serialize(it.representation))
                }
                _token.postValue(newToken)
            }
            _updatingToken.value = false
        }
    }

    fun spend(amount: Int) {
        uiScope.launch {
            _updatingToken.value = true
            withContext(Dispatchers.IO) {
                Timber.i("Run spend-deduct to update tokens value")
                try {

                    val userKeyPair = UserKeyPair(_userPublicKey.value, _userSecretKey.value)
                    val providerKeyPair =
                        ProviderKeyPair(_providerSecretKey.value, _providerPublicKey.value)
                    val tid = incentiveSystem.pp.bg.zn.uniformlyRandomElement
                    val spendRequest = incentiveSystem.generateSpendRequest(
                        _token.value,
                        _providerPublicKey.value,
                        BigInteger.valueOf(amount.toLong()),
                        userKeyPair,
                        tid
                    )
                    val spendResponseTuple = incentiveSystem.generateSpendRequestResponse(
                        spendRequest,
                        providerKeyPair,
                        BigInteger.valueOf(amount.toLong()),
                        tid
                    )
                    val newToken = incentiveSystem.handleSpendRequestResponse(
                        spendResponseTuple.spendResponse,
                        spendRequest,
                        _token.value,
                        BigInteger.valueOf(amount.toLong()),
                        _providerPublicKey.value,
                        userKeyPair
                    )
                    val jsonConverter = JSONConverter()
                    newToken?.let {
                        cryptoRepository.setToken(jsonConverter.serialize(it.representation))
                    }
                    _token.postValue(newToken)
                } catch (exception: IllegalArgumentException) {
                    _exceptionToast.postValue("Invalid spend request!")
                }
            }
            _updatingToken.value = false
        }
    }

    private fun loadCryptoAssets() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                // Setup cryptographic assets (some will be retrieved via http request later)
                Timber.i("Loading crypto assets")
                val jsonConverter = JSONConverter()
                val pp =
                    IncentivePublicParameters(jsonConverter.deserialize(cryptoRepository.getPublicParameters()))
                val ppk = ProviderPublicKey(
                    jsonConverter.deserialize(cryptoRepository.getProviderPublicKey()),
                    pp.spsEq,
                    pp.bg.g1
                )
                val psk = ProviderSecretKey(
                    jsonConverter.deserialize(cryptoRepository.getProviderSecretKey()),
                    pp.spsEq,
                    pp.bg.zn,
                    pp.prfToZn
                )
                val upk = UserPublicKey(
                    jsonConverter.deserialize(cryptoRepository.getUserPublicKey()),
                    pp.bg.g1
                )
                val usk = UserSecretKey(
                    jsonConverter.deserialize(cryptoRepository.getUserSecretKey()),
                    pp.bg.zn,
                    pp.prfToZn
                )

                _incentivePublicParameters.postValue(pp)
                _providerPublicKey.postValue(ppk)
                _providerSecretKey.postValue(psk)
                _userPublicKey.postValue(upk)
                _userSecretKey.postValue(usk)

                val tokenString = cryptoRepository.getToken()
                if (tokenString != "") {
                    Timber.i("Loading Token")
                    val token = Token(
                        jsonConverter.deserialize(tokenString),
                        pp
                    )
                    _token.postValue(token)
                }
                Timber.i("Finished loading crypto assets")
            }
            Timber.i("Setup incentive system")
            incentiveSystem = IncentiveSystem(_incentivePublicParameters.value)
            _updatingToken.value = false
        }
    }

    fun toastShown() {
        _exceptionToast.value = ""
    }
}