package com.example.smartbasket  // Add this line

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Done
import java.util.*

// Mock data class with VAT category
data class BasketItem(
    val name: String,
    val price: Double,      // Price without VAT
    val quantity: Int,
)

// ViewModel to hold items
class ReceiptViewModel : ViewModel() {
    val items = listOf(
        BasketItem("Apple", 1.99, 2),
        BasketItem("Milk", 3.49, 1),
        BasketItem("Bread", 2.99, 1),
        BasketItem("Novel", 14.99, 1),
        BasketItem("Eggs", 2.49, 12),
        BasketItem("Butter", 3.99, 2),
        BasketItem("Cheese", 4.99, 1),
        BasketItem("Chicken", 8.99, 1),
        BasketItem("Rice", 5.99, 2),
        BasketItem("Pasta", 1.99, 3),
        BasketItem("Tomatoes", 2.99, 6),
        BasketItem("Potatoes", 3.49, 10)
    )

    val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("EUR") // Euro formatting
        maximumFractionDigits = 2
    }

    val total: Double
        get() = items.sumOf { it.price * it.quantity }

    fun getTopItemsByQuantity(): List<Pair<String, Int>> {
        return items.groupBy { it.name }
            .mapValues { (_, items) -> items.sumOf { it.quantity } }
            .entries.sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }
    }

    fun getTopItemsBySales(): List<Pair<String, Double>> {
        return items.groupBy { it.name }
            .mapValues { (_, items) -> items.sumOf { it.price * it.quantity } }
            .entries.sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }
    }

    fun getCategoryDistribution(): Map<String, Double> {
        // Mock categories - you would need to add real categories to BasketItem
        val categories = mapOf(
            "Fruit" to listOf("Apple"),
            "Vegetables" to listOf("Tomatoes", "Potatoes"),
            "Dairy" to listOf("Milk", "Butter", "Cheese"),
            "Bakery" to listOf("Bread"),
            "Meat" to listOf("Chicken"),
            "Other Groceries" to listOf("Rice", "Pasta", "Eggs")
        )

        return categories.mapValues { (_, items) ->
            this.items.filter { items.contains(it.name) }
                .sumOf { it.price * it.quantity }
        }.filterValues { it > 0 }
    }
}

// Modified ReceiptScreen with payment integration
@Composable
fun ReceiptScreen(
    onBack: () -> Unit = {},
    shoppingListViewModel: ShoppingListViewModel  // Add this parameter
) {
    val receiptViewModel: ReceiptViewModel = viewModel()
    val paymentViewModel: PaymentViewModel = viewModel()
    val walletViewModel: WalletViewModel = viewModel() // Add this line
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onBack,  // This was the missing piece
            modifier = Modifier.padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Back to Menu", color = Color.DarkGray)
        }

        HeaderSection(paymentViewModel)

        LazyColumn(modifier = Modifier.weight(1f).heightIn(max = 500.dp)) {
            items(receiptViewModel.items) { item ->
                val isInShoppingList = shoppingListViewModel.shoppingList.value.any { listItem ->
                    listItem.name == item.name && item.quantity >= listItem.quantity
                }

                ReceiptItemRow(
                    item = item,
                    currencyFormat = receiptViewModel.currencyFormat,
                    isInShoppingList = isInShoppingList
                )
                Divider(color = Color.LightGray)
            }
        }

        ReceiptTotalRow("Total", receiptViewModel.total, receiptViewModel.currencyFormat)
        PaymentSection(
            receiptViewModel = receiptViewModel,
            paymentViewModel = paymentViewModel,
            walletViewModel = walletViewModel  // Pass it here
        )
    }
}

@Composable
private fun PaymentSection(
    receiptViewModel: ReceiptViewModel,
    paymentViewModel: PaymentViewModel,
    walletViewModel: WalletViewModel
) {
    if (!paymentViewModel.showPayment) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Wallet Balance: €${"%.2f".format(walletViewModel.balance)}",
                style = MaterialTheme.typography.bodyLarge
            )

            if (walletViewModel.balance < 20) {
                Text("Low balance!", color = Color.Red)
            }

            Button(
                onClick = { paymentViewModel.startPayment(PaymentContext.PURCHASE, receiptViewModel.total) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("PAY NOW", fontSize = 18.sp)
            }
        }
    } else {
        PaymentStatus(
            viewModel = paymentViewModel,
            total = receiptViewModel.total,
            walletViewModel = walletViewModel,
            context = PaymentContext.PURCHASE  // Explicitly set payment context
        )
    }
}

@Composable
fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: PaymentViewModel,
    walletViewModel: WalletViewModel,
    context: PaymentContext
) {
    val amount = when (context) {
        PaymentContext.ADD_FUNDS -> viewModel.amountToAdd.toDoubleOrNull() ?: 0.0
        else -> viewModel.totalAmount
    }
    val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("EUR")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm ${if (context == PaymentContext.ADD_FUNDS) "Funds Addition" else "Payment"}") },
        text = {
            Column {
                Text("Confirm ${if (context == PaymentContext.ADD_FUNDS) "adding" else "payment of"} ${currencyFormat.format(amount)}?")
                if (context == PaymentContext.PURCHASE && walletViewModel.balance < amount) {
                    Text("Insufficient funds!", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = context != PaymentContext.PURCHASE || walletViewModel.balance >= amount,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Confirm",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },

        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                )
            ) {
                Text("Cancel",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
            }
        }
    )
}

@Composable
private fun HeaderSection(paymentViewModel: PaymentViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Your Receipt",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ReceiptItemRow(
    item: BasketItem,
    currencyFormat: NumberFormat,
    isInShoppingList: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isInShoppingList) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "In shopping list",
                    tint = Color.Green,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Column {
                Text(
                    text = "${item.quantity}x ${item.name}",
                    fontSize = 16.sp,
                    color = if (isInShoppingList) Color.Green else Color.Unspecified
                )
                Text(
                    text = "${item.name}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        Text(
            text = currencyFormat.format(item.price * item.quantity),
            fontSize = 16.sp
        )
    }
}

@Composable
fun ReceiptTotalRow(label: String, amount: Double, currencyFormat: NumberFormat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = currencyFormat.format(amount),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
