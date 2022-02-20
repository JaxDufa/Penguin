package com.my.penguin.presentation.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.penguin.NetworkProvider
import com.my.penguin.data.ExchangeRateRepository
import com.my.penguin.data.Result
import com.my.penguin.data.model.ExchangeRates
import com.my.penguin.presentation.models.Country
import com.my.penguin.presentation.models.CurrencyBinaryValue
import com.my.penguin.presentation.models.Transaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ExchangeRateRepository,
    private val networkProvide: NetworkProvider
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState>()
    val stateViewState: LiveData<ViewState> by ::_viewState

    private val _currencyBinaryFinalValue = MutableLiveData<CurrencyBinaryValue>()
    val currencyBinaryFinalValue: LiveData<CurrencyBinaryValue> by ::_currencyBinaryFinalValue

    var selectedCountry: Country? = null
    private val countries: List<Country> = listOf(
        Country.Kenya,
        Country.Nigeria,
        Country.Tanzania,
        Country.Uganda
    )

    private var transaction: Transaction? = null

    init {
        onTryAgain()
    }

    // region - Public
    fun onTryAgain() {
        viewModelScope.launch {
            _viewState.postValue(ViewState.Loading)
            when (val result = repository.loadExchangeRates()) {
                is Result.Success -> {
                    loadExchangeRates(result.data)
                    postViewState(
                        ViewState.Initial(
                            countries.map { it.name }
                        )
                    )
                }
                is Result.Error -> {
                    if (networkProvide.isConnected) {
                        postViewState(ViewState.Error(ErrorType.UnknownError))
                    } else {
                        postViewState(ViewState.Error(ErrorType.NetworkError))
                    }
                }
            }
        }
    }

    fun onCountrySelected(position: Int) {
        selectedCountry = countries.getOrNull(position)
        selectedCountry?.let {
            _viewState.postValue(ViewState.Default(it))
        }
    }

    fun onAmountChanged(amount: String) {
        val country = selectedCountry ?: return
        if (amount.isBlank()) {
            updateCurrentBinary(country.currencyPrefix, "")
            return
        }
        val valueInCurrentExchange = amount.toDecimal() * country.exchangeRate
        val binaryCurrentExchange = Integer.toBinaryString(valueInCurrentExchange.toInt())
        updateCurrentBinary(country.currencyPrefix, binaryCurrentExchange)
    }

    fun onSendAction(firstName: String, lastName: String, phoneNumber: String, amount: String) {
        val country = selectedCountry ?: return
        val valuesToValidate = handleValidation(firstName, lastName, phoneNumber)
        val hasInvalidValue = valuesToValidate.any { !it }
        if (hasInvalidValue) {
            _viewState.postValue(
                ViewState.InputFieldError(
                    !valuesToValidate[0],
                    !valuesToValidate[1],
                    !valuesToValidate[2]
                )
            )
        } else {
            transaction =
                Transaction(firstName, lastName, amount, country.phonePrefix, phoneNumber).also {
                    _viewState.postValue(ViewState.Confirm(it))
                }
        }
    }

    fun onConfirmAction() {
        fakeSend()
    }
    // endregion

    // region - Private
    private fun handleValidation(
        firstName: String,
        lastName: String,
        phoneNumber: String
    ): List<Boolean> {
        return listOf(
            firstName.isNotBlank(),
            lastName.isNotBlank(),
            phoneNumber.length == selectedCountry?.phoneNumberDigits
        )
    }

    private fun updateCurrentBinary(prefix: String, value: String) {
        _currencyBinaryFinalValue.postValue(
            CurrencyBinaryValue(
                prefix,
                value
            )
        )
    }

    private fun loadExchangeRates(exchangeRates: ExchangeRates) {
        countries.forEach {
            it.exchangeRate = when (it) {
                Country.Kenya -> exchangeRates.kenya
                Country.Nigeria -> exchangeRates.nigeria
                Country.Tanzania -> exchangeRates.tanzania
                Country.Uganda -> exchangeRates.uganda
            }
        }
    }

    private fun postViewState(state: ViewState) {
        _viewState.postValue(state)
    }

    private fun fakeSend() {
        viewModelScope.launch {
            _viewState.postValue(ViewState.Loading)
            delay(5000)

            transaction?.let {
                _viewState.postValue(
                    ViewState.Complete(it)
                )
            }
            transaction = null
        }
    }
    // endregion

    private fun String.toDecimal(): Long = toLong(2)
}