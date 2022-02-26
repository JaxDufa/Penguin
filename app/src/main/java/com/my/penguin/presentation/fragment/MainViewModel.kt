package com.my.penguin.presentation.fragment

import androidx.annotation.IntRange
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.penguin.NetworkProvider
import com.my.penguin.data.ExchangeRateRepository
import com.my.penguin.data.Result
import com.my.penguin.data.model.ExchangeRates
import com.my.penguin.presentation.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ExchangeRateRepository,
    private val networkProvider: NetworkProvider
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> by ::_viewState

    private val _currencyBinaryFinalValue = MutableLiveData<RecipientCurrencyBinaryValue>()
    val recipientCurrencyBinaryValue: LiveData<RecipientCurrencyBinaryValue> by ::_currencyBinaryFinalValue

    var selectedCountry: Country? = null
    private var transaction: Transaction? = null

    private val countries: List<Country> = listOf(
        Country.Kenya,
        Country.Nigeria,
        Country.Tanzania,
        Country.Uganda
    )

    init {
        onTryAgain()
    }

    // region - Public
    fun onTryAgain() {
        viewModelScope.launch {
            postViewState(ViewState.Loading)
            when (val result = repository.loadExchangeRates()) {
                is Result.Success -> onSuccessResponse(result.data)
                is Result.Error -> onErrorResponse()
            }
        }
    }

    private fun onSuccessResponse(exchangeRates: ExchangeRates) {
        loadExchangeRates(exchangeRates)
        postViewState(
            ViewState.Initial(
                countries.map { it.name }
            )
        )
    }

    private fun onErrorResponse() {
        val viewState = if (networkProvider.isConnected) {
            ViewState.GeneralError(ErrorType.UnknownError)
        } else {
            ViewState.GeneralError(ErrorType.NetworkError)
        }
        postViewState(viewState)
    }

    fun onCountrySelected(@IntRange(from = 0, to = 3) position: Int) {
        selectedCountry = countries.getOrNull(position)?.also {
            postViewState(ViewState.Default(it))
        }
    }

    fun onAmountChanged(amount: String) {
        runSafeCountryBlock {
            if (amount.isBlank()) {
                updateCurrentBinary(it.currencyPrefix)
            } else {
                val valueInRecipientExchange = it.exchangeRate.apply(amount.toDecimal())
                val binaryInRecipientExchange = valueInRecipientExchange.toBinaryString()
                updateCurrentBinary(it.currencyPrefix, binaryInRecipientExchange)
            }
        }
    }

    fun onSendAction(firstName: String, lastName: String, phoneNumber: String, amount: String) {
        runSafeCountryBlock {
            val phoneAndPrefix = CountryPhone(it.phonePrefix, phoneNumber)
            val fieldsStatus = InputFieldsStatus(
                firstName.isNotBlank(),
                lastName.isNotBlank(),
                phoneAndPrefix.isValid(it.phoneNumberDigits)
            )
            if (fieldsStatus.isAnyFieldInvalid) {
                postViewState(ViewState.InputFieldError(fieldsStatus))
            } else {
                confirmTransaction(Name(firstName, lastName), phoneAndPrefix, amount)
            }
        }
    }

    fun onConfirmAction() {
        fakeSendRequest()
    }
    // endregion

    // region - Private
    private fun confirmTransaction(
        name: Name,
        countryPhone: CountryPhone,
        amount: String
    ) {
        transaction =
            Transaction(
                name.fullName,
                amount,
                countryPhone.fullPhoneNumber
            ).also {
                postViewState(ViewState.Confirm(it))
            }
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

    private fun fakeSendRequest() {
        viewModelScope.launch {
            postViewState(ViewState.Loading)

            // Delay to simulate a network request
            delay(5000)

            transaction?.let {
                postViewState(ViewState.Complete(it))
            }
            transaction = null
        }
    }

    private fun runSafeCountryBlock(block: (country: Country) -> Unit) {
        selectedCountry?.let {
            block(it)
        }
    }

    private fun updateCurrentBinary(prefix: String, value: String = "") {
        _currencyBinaryFinalValue.postValue(
            RecipientCurrencyBinaryValue(
                prefix,
                value
            )
        )
    }

    private fun postViewState(state: ViewState) {
        _viewState.postValue(state)
    }
    // endregion

    // region - Extensions
    private fun String.toDecimal(): Long = toLong(2)

    private fun Float.toBinaryString(): String = Integer.toBinaryString(this.toInt())
    // endregion
}