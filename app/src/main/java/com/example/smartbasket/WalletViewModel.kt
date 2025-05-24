package com.example.smartbasket

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider

// WalletViewModel.kt
class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)

    var balance by mutableStateOf(sharedPrefs.getFloat("balance", 100f).toDouble())
        private set

    fun addFunds(amount: Double) {
        balance += amount
        saveBalance()
    }

    fun deductAmount(amount: Double): Boolean {
        return if (balance >= amount) {
            balance -= amount
            saveBalance()
            true
        } else {
            false
        }
    }

    private fun saveBalance() {
        sharedPrefs.edit().putFloat("balance", balance.toFloat()).apply()
    }
}

class WalletViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WalletViewModel(application) as T
    }
}


