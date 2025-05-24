package com.example.smartbasket

// Add these imports at the top
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PaymentContext { PURCHASE, ADD_FUNDS }

// Add new PaymentViewModel class
class PaymentViewModel : ViewModel() {
    companion object {
        const val DEMO_CARD_NUMBER = "4242424242424242"
        const val DEMO_EXPIRY = "12/30"
        const val DEMO_CVC = "123"
    }

    // Add these missing properties
    var showPayment by mutableStateOf(false)
    var cardNumber by mutableStateOf("")
    var expiryDate by mutableStateOf("")
    var cvc by mutableStateOf("")
    var paymentState by mutableStateOf<PaymentState>(PaymentState.Entry)
    var totalAmount by mutableStateOf(0.0)
    var isSending by mutableStateOf(false)

    // Existing properties
    var paymentContext by mutableStateOf(PaymentContext.PURCHASE)
    var amountToAdd by mutableStateOf("")

    fun handlePayment(walletViewModel: WalletViewModel, amount: Double) {
        // Changed from deductBalance to deductAmount
        val success = walletViewModel.deductAmount(amount)

        // Update payment state based on success
        paymentState = if (success) {
            PaymentState.Success("BLUETOOTH-${System.currentTimeMillis()}")
        } else {
            PaymentState.Error("Payment deduction failed")
        }
    }

    // Keep the existing sealed class and functions
    sealed class PaymentState {
        object Entry : PaymentState()
        object Processing : PaymentState()
        data class Success(val transactionId: String) : PaymentState()
        data class Error(val message: String) : PaymentState()
    }

    fun isValidDemoCard(): Boolean {
        return cardNumber == DEMO_CARD_NUMBER &&
                expiryDate == DEMO_EXPIRY &&
                cvc == DEMO_CVC
    }

    fun cancelPayment() {
        showPayment = false
        resetPayment()
    }

    private fun resetPayment() {
        cardNumber = ""
        expiryDate = ""
        cvc = ""
        amountToAdd = ""
        paymentState = PaymentState.Entry
    }

    // Rest of your existing functions
    fun startPayment(context: PaymentContext, total: Double = 0.0) {
        showPayment = true
        paymentContext = context
        totalAmount = total
        paymentState = PaymentState.Entry
    }

    fun processPayment(walletViewModel: WalletViewModel) {
        viewModelScope.launch {
            paymentState = PaymentState.Processing
            delay(1000)

            if (!isValidDemoCard()) {
                paymentState = PaymentState.Error("Invalid card. Please use the demo card details.")
                return@launch
            }

            when (paymentContext) {
                PaymentContext.PURCHASE -> {
                    val success = walletViewModel.deductAmount(totalAmount)
                    paymentState = if (success) {
                        PaymentState.Success("PURCHASE-${System.currentTimeMillis()}")
                    } else {
                        PaymentState.Error("Insufficient funds")
                    }
                }
                PaymentContext.ADD_FUNDS -> {
                    val amount = amountToAdd.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        walletViewModel.addFunds(amount)  // Now passing the amount parameter
                        paymentState = PaymentState.Success("ADD-FUNDS-${System.currentTimeMillis()}")
                        amountToAdd = ""
                    } else {
                        paymentState = PaymentState.Error("Invalid amount")
                    }
                }
            }
        }
    }
}

// Add new composables
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentForm(viewModel: PaymentViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Payment Details",
            style = MaterialTheme.typography.headlineSmall
        )

        DemoCardHelper(viewModel)

        OutlinedTextField(
            value = viewModel.cardNumber,
            onValueChange = { viewModel.cardNumber = it.filter { c -> c.isDigit() } },
            label = { Text("Card Number") },
            placeholder = { Text("4242 4242 4242 4242") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = CreditCardFilter(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Credit Card"
                )
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = viewModel.expiryDate,
                onValueChange = { viewModel.expiryDate = it.filter { c -> c.isDigit() || c == '/' } },
                label = { Text("MM/YY") },
                placeholder = { Text("12/30") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = viewModel.cvc,
                onValueChange = { viewModel.cvc = it.filter { c -> c.isDigit() } },
                label = { Text("CVC") },
                placeholder = { Text("123") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DemoCardHelper(viewModel: PaymentViewModel) {
    Button(
        onClick = {
            viewModel.cardNumber = PaymentViewModel.DEMO_CARD_NUMBER
            viewModel.expiryDate = PaymentViewModel.DEMO_EXPIRY
            viewModel.cvc = PaymentViewModel.DEMO_CVC
        },
        modifier = Modifier
            .height(40.dp),        // Match Back to Menu's height
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.LightGray,
            contentColor = Color.DarkGray
        )
    ) {
        Text("Fill Demo Card")
    }
}

@Composable
fun PaymentStatus(viewModel: PaymentViewModel, total: Double, walletViewModel: WalletViewModel, context: PaymentContext) {
    var showConfirmation by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = viewModel.paymentState) {
            is PaymentViewModel.PaymentState.Entry -> {
                Column {
                    CardForm(
                        viewModel = viewModel,
                        walletViewModel = walletViewModel,
                        context = context
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { showConfirmation = true },
                            modifier = Modifier.weight(1f).height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text(
                                // Change this line to show different text based on context
                                when(context) {
                                    PaymentContext.ADD_FUNDS -> "Add Funds"
                                    else -> "Submit Payment"
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = { viewModel.cancelPayment() },
                            modifier = Modifier.weight(1f).height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                        ) {
                            Text(
                                "Cancel",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                        }
                    }

                    if (showConfirmation) {
                        ConfirmationDialog(
                            onConfirm = {
                                showConfirmation = false
                                viewModel.processPayment(walletViewModel)
                            },
                            onDismiss = { showConfirmation = false },
                            viewModel = viewModel,
                            walletViewModel = walletViewModel,  // Add this line
                            context = context
                        )
                    }
                }
            }

            is PaymentViewModel.PaymentState.Processing -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is PaymentViewModel.PaymentState.Success -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Done,
                        "Success",
                        tint = Color.Green,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("Payment Successful!", style = MaterialTheme.typography.headlineSmall)
                    Text("Transaction ID: ${state.transactionId}")
                    Button(
                        onClick = {
                            viewModel.cancelPayment()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // Green color
                            contentColor = Color.White
                        )
                    ) {
                        Text("Done")
                    }
                }
            }

            is PaymentViewModel.PaymentState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Error,
                        "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("Payment Failed", style = MaterialTheme.typography.headlineSmall)
                    Text(state.message)
                    Button(
                        onClick = {
                            viewModel.paymentState = PaymentViewModel.PaymentState.Entry
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 35.dp)
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // Green color
                            contentColor = Color.White
                        )
                    ) {
                        Text("Try Again")
                    }
                    Button(
                        onClick = { viewModel.cancelPayment() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 35.dp)
                            .padding(top = 5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Cancel Payment", color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

// Replace this placeholder
@Composable
fun CircularProgressIndicator() {
    CircularProgressIndicator()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardForm(
    viewModel: PaymentViewModel,
    walletViewModel: WalletViewModel,
    modifier: Modifier = Modifier,
    context: PaymentContext = PaymentContext.PURCHASE
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),  // Add scrolling for longer forms
        verticalArrangement = Arrangement.spacedBy(20.dp)  // Increased spacing
    ) {
        Text(
            text = when(context) {
                PaymentContext.ADD_FUNDS -> "Add Funds with Card"
                else -> "Payment Details"
            },
            style = MaterialTheme.typography.headlineSmall
        )

        // Demo card button with proper spacing
        Column(modifier = Modifier.fillMaxWidth()) {
            DemoCardHelper(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (context == PaymentContext.ADD_FUNDS) {
            OutlinedTextField(
                value = viewModel.amountToAdd,
                onValueChange = { viewModel.amountToAdd = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount to Add") },
                placeholder = { Text("Enter amount (e.g., 100.00)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Amount"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = viewModel.cardNumber,
            onValueChange = { viewModel.cardNumber = it.filter { c -> c.isDigit() } },
            label = { Text("Card Number") },
            placeholder = { Text("4242 4242 4242 4242") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Credit Card"
                )
            },
            visualTransformation = CreditCardFilter(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.expiryDate,
                onValueChange = { viewModel.expiryDate = it.filter { c -> c.isDigit() || c == '/' } },
                label = { Text("MM/YY") },
                placeholder = { Text("12/30") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = viewModel.cvc,
                onValueChange = { viewModel.cvc = it.filter { c -> c.isDigit() } },
                label = { Text("CVC") },
                placeholder = { Text("123") },
                modifier = Modifier.weight(1f)
            )
        }

    }
}