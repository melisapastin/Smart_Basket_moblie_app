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
import kotlinx.serialization.Serializable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.util.*


// Mock data class with VAT category
data class BasketItem(
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String  // Added category field
)

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val weight: Int
) {
    fun toBasketItem(quantity: Int) = BasketItem(
        name = name,
        price = price,
        quantity = quantity,
        category = category
    )
}

// ViewModel to hold items
class ReceiptViewModel : ViewModel() {
    var items by mutableStateOf<List<BasketItem>>(emptyList())
        private set

    fun clearItems() {
        items = emptyList()
    }

    init {
        fetchCloudData()
    }

    private fun fetchCloudData() {
        viewModelScope.launch {
            try {
                val products = withContext(Dispatchers.IO) {
                    val url = URL("https://smartcart1.blob.core.windows.net/products/recognized_cart.json?sp=r&st=2025-05-07T11:28:08Z&se=2025-06-30T19:28:08Z&spr=https&sv=2024-11-04&sr=b&sig=R21ZlTEcdDbjxQdj23y0dc3vV1NcVH9Df3%2B8%2Bw%2Fc6Ew%3D\n")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.apply {
                        requestMethod = "GET"
                        connectTimeout = 5000
                        readTimeout = 5000
                    }

                    // Fixed: Use connection.responseCode
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        // Fixed: Use connection.inputStream
                        val json = connection.inputStream.bufferedReader().use { it.readText() }
                        Json.decodeFromString<List<Product>>(json)
                    } else emptyList()
                }

                items = products.groupBy { it.id }
                    .map { (_, items) ->
                        BasketItem(
                            name = items.first().name,
                            price = items.first().price,
                            quantity = items.size,
                            category = items.first().category  // Add category here
                        )
                    }
            } catch (e: Exception) {
                // Handle errors (consider updating UI state here)
                e.printStackTrace()
            }
        }
    }

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
        // Group items by their actual category from Product data
        return items.groupBy { it.category }
            .mapValues { (_, items) ->
                items.sumOf { it.price * it.quantity }
            }
            .filterValues { it > 0 }
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

    ///////////// EMPTIES THE LIST AFTER PAYMENT
    LaunchedEffect(paymentViewModel.paymentState) {
        if (paymentViewModel.paymentState is PaymentViewModel.PaymentState.Success &&
            paymentViewModel.paymentContext == PaymentContext.PURCHASE) {
            receiptViewModel.clearItems()
        }
    }
    ///////////////////////////////////////////

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onBack,  // This was the missing piece
            modifier = Modifier.padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Back to Menu", color = Color.DarkGray)
        }

        HeaderSection(paymentViewModel)

        if (receiptViewModel.items.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.weight(1f).heightIn(max = 500.dp)) {
                items(receiptViewModel.items) { item ->
                    val isInShoppingList =
                        shoppingListViewModel.shoppingList.value.any { listItem ->
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
    val currentContext = LocalContext.current
    val activity = currentContext as ComponentActivity
    val bluetoothManager = remember { BluetoothManager(currentContext) }

    val permissions = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.all { it.value }) {
            // All permissions granted
            viewModel.handlePayment(walletViewModel, viewModel.totalAmount)
            bluetoothManager.sendSignal('O')
            onConfirm()
        } else {
            Toast.makeText(currentContext, "Permissions required for Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Payment") },
        text = {
            Column {
                Text("Confirm payment of ${NumberFormat.getCurrencyInstance().format(viewModel.totalAmount)}?")
                if (walletViewModel.balance < viewModel.totalAmount) {
                    Text("Insufficient funds!", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (permissions.all {
                            ContextCompat.checkSelfPermission(
                                currentContext,
                                it
                            ) == PackageManager.PERMISSION_GRANTED
                        }) {
                        viewModel.handlePayment(walletViewModel, viewModel.totalAmount)
                        bluetoothManager.sendSignal('O')
                        onConfirm()
                    } else {
                        permissionLauncher.launch(permissions)
                    }
                },
                enabled = walletViewModel.balance >= viewModel.totalAmount,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Cancel", color = Color.DarkGray)
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