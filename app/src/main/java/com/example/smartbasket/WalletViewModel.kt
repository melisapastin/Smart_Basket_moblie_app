package com.example.smartbasket

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// WalletViewModel.kt
class WalletViewModel : ViewModel() {
    var balance by mutableStateOf(100.0) // Starting balance
    var showAddFunds by mutableStateOf(false)
    var addAmount by mutableStateOf("")

    fun addFunds(amount: Double) {
        balance += amount
    }

    fun deductAmount(amount: Double): Boolean {
        if (balance >= amount) {
            balance -= amount
            return true
        }
        return false
    }
}

@Composable
fun PresetAmountsRow(walletViewModel: WalletViewModel) {
    Row {
        listOf(10.0, 20.0, 50.0).forEach { amount ->
            Button(onClick = { walletViewModel.addAmount = amount.toString() }) {
                Text("€$amount")
            }
        }
    }
}