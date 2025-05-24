package com.example.smartbasket

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WalletScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val walletViewModel: WalletViewModel = viewModel(
        factory = WalletViewModelFactory(context.applicationContext as Application)
    )
    val paymentViewModel: PaymentViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Back to Menu", color = Color.DarkGray)
        }

        Text(
            "Current Balance: €${"%.2f".format(walletViewModel.balance)}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(
            onClick = {
                paymentViewModel.startPayment(PaymentContext.ADD_FUNDS)
                paymentViewModel.amountToAdd = "" // Reset amount field
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Add Funds", fontSize = 18.sp)
        }

        if (paymentViewModel.showPayment) {
            PaymentStatus(
                viewModel = paymentViewModel,
                total = paymentViewModel.amountToAdd.toDoubleOrNull() ?: 0.0, // Add this line
                walletViewModel = walletViewModel,
                context = PaymentContext.ADD_FUNDS
            )
        }
    }
}

data class Transaction(
    val amount: Double,
    val type: String, // "ADD" or "PAYMENT"
    val timestamp: Long
)