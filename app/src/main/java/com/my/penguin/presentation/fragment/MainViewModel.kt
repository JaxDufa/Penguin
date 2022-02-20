package com.my.penguin.presentation.fragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.penguin.data.ExchangeRateRepository
import com.my.penguin.data.Result
import com.my.penguin.data.model.ExchangeRates
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ExchangeRateRepository
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState>()
    val stateViewState: LiveData<ViewState> by ::_viewState

    var selectedCountry: Country? = null
    private val countries: List<Country> = listOf(
        Country.Kenya,
        Country.Nigeria,
        Country.Tanzania,
        Country.Uganda
    )

    init {
        onTryAgain()
    }

    fun onTryAgain() {
        viewModelScope.launch {
            _viewState.postValue(ViewState.Loading)
            when (val result = repository.loadExchangeRates()) {
                is Result.Success -> {
                    loadExchangeRates(result.data)
                    postViewState(ViewState.Initial(countries))
                }
                is Result.Error -> postViewState(ViewState.Error(ErrorType.UnknownError))
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
        if (amount.isBlank()) return
        Log.d("TAG", amount.toDecimal().toString())
        val value = amount.toDecimal() * (selectedCountry?.exchangeRate ?: 1.0f)
        Log.d("TAG", "$value")
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

    private fun String.toDecimal(): Long = toLong(2)
}