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

// ShoppingListScreen.kt
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = shoppingListViewModel.newItemName.value,  // Fixed
                onValueChange = { shoppingListViewModel.newItemName.value = it },
                modifier = Modifier.weight(1f),
                label = { Text("Item name") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = shoppingListViewModel.newItemQuantity.value,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\$"))) {
                        shoppingListViewModel.newItemQuantity.value = newValue // Fixed variable name
                    }
                },
                modifier = Modifier.width(80.dp),
                label = { Text("Qty") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            IconButton(
                onClick = { shoppingListViewModel.addItem() }
            ) {
                Icon(Icons.Default.Add, "Add item")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(
                shoppingListViewModel.shoppingList.value,  // Fixed
                key = { item -> item.id }
            )  { item ->
                // Check if basket contains this item with sufficient quantity
                val isInBasket = receiptViewModel.items.any { basketItem ->
                    basketItem.name == item.name &&
                            basketItem.quantity >= item.quantity
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
                        onClick = { shoppingListViewModel.removeItem(item.id) }  // Fixed
                    ) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                    }
                }
                Divider(color = Color.LightGray)
            }
        }
    }
}