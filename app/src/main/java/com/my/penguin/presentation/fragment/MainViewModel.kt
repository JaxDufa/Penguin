package com.my.penguin.presentation.fragment

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.penguin.data.ExchangeRateRepository
import com.my.penguin.data.Result
import com.my.penguin.data.model.ExchangeRates
import kotlinx.coroutines.launch

enum class Country {
    KENYA, NIGERIA, TANZANIA, UGANDA
}

class MainViewModel(
    private val repository: ExchangeRateRepository
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState>()
    val stateViewState: LiveData<ViewState> by ::_viewState

    var selectedCountry: Country? = null

    init {
       loadExchangeRates()
    }

    fun loadExchangeRates() {
        viewModelScope.launch {
            _viewState.postValue(ViewState.Loading)
            when (val result = repository.loadExchangeRates()) {
                is Result.Success -> postViewState(ViewState.CurrentRate(result.data.toCountryRate()))
                is Result.Error -> postViewState(ViewState.Error(ErrorType.UnknownError))
            }
        }
    }

    private fun ExchangeRates.toCountryRate() : Float {
        return when(selectedCountry) {
            Country.KENYA -> kenya
            Country.NIGERIA -> nigeria
            Country.TANZANIA -> tanzania
            Country.UGANDA -> uganda
            null -> 0f
        }
    }

    private fun postViewState(state: ViewState) {
        _viewState.postValue(state)
    }
}