package com.my.penguin

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkProvider(private val connectivityManager: ConnectivityManager) {

    val isConnected: Boolean
        get() = connectivityManager.isNetworkAvailable()

    private fun ConnectivityManager.isNetworkAvailable(): Boolean {
        val capabilities = activeNetwork ?: return false
        val activeNetwork = getNetworkCapabilities(capabilities) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}