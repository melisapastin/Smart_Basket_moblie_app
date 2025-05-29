package com.example.smartbasket

// ShoppingListScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Done

@Composable
fun ShoppingListScreen(
    shoppingListViewModel: ShoppingListViewModel,
    onBack: () -> Unit
) {
    val receiptViewModel: ReceiptViewModel = viewModel()

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

        Spacer(modifier = Modifier.height(16.dp))

        // Top half: Available products
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxWidth()
        ) {
            Text("Available Products", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            when {
                shoppingListViewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                shoppingListViewModel.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${shoppingListViewModel.error}", color = Color.Red)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(shoppingListViewModel.cloudProducts) { product ->
                            var quantity by remember { mutableStateOf("1") }

                            ProductItem(
                                product = product,
                                quantity = quantity,
                                onQuantityChange = { quantity = it },
                                onAddClick = {
                                    val qty = quantity.toIntOrNull() ?: 1
                                    shoppingListViewModel.addCloudProduct(product, qty)
                                }
                            )
                            Divider(color = Color.LightGray)
                        }
                    }
                }
            }
        }

        // Horizontal divider
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(vertical = 8.dp),
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom half: Shopping list
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text("Your Shopping List", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(shoppingListViewModel.shoppingList.value) { item ->
                    ShoppingListItemRow(
                        item = item,
                        onRemove = { shoppingListViewModel.removeItem(it) },
                        receiptItems = receiptViewModel.items
                    )
                    Divider(color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun ShoppingListItemRow(
    item: ShoppingListItem,
    onRemove: (Int) -> Unit,
    receiptItems: List<BasketItem>
) {
    val isInBasket = receiptItems.any { basketItem ->
        basketItem.name == item.name && basketItem.quantity >= item.quantity
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isInBasket) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "In basket",
                tint = Color.Green,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Text(
            text = "${item.quantity}x ${item.name}",
            modifier = Modifier.weight(1f),
            color = if (isInBasket) Color.Green else Color.Unspecified
        )
        IconButton(
            onClick = { onRemove(item.id) }
        ) {
            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge)
            Text("€${"%.2f".format(product.price)}", color = Color.Gray)
        }

        OutlinedTextField(
            value = quantity,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\$"))) {
                    onQuantityChange(it)
                }
            },
            modifier = Modifier.width(80.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = onAddClick,
            modifier = Modifier.padding(start = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
        Text("Add")
    }
    }
}