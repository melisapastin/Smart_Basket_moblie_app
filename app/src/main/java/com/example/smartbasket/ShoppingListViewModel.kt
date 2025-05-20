package com.example.smartbasket

// ShoppingListViewModel.kt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class ShoppingListItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val price: Double
)

// ShoppingListViewModel.kt
class ShoppingListViewModel : ViewModel() {
    val shoppingList = mutableStateOf(listOf<ShoppingListItem>())
    val newItemName = mutableStateOf("")
    val newItemQuantity = mutableStateOf("")
    private var nextId by mutableStateOf(0)
    val newItemPrice = mutableStateOf("")

    val totalEstimatedCost: Double
        get() = shoppingList.value.sumOf { it.quantity * it.price }

    fun addItem() {
        val name = newItemName.value.trim()
        val quantity = newItemQuantity.value.toIntOrNull() ?: 0
        val price = newItemPrice.value.toDoubleOrNull() ?: 0.0

        if (name.isNotBlank() && quantity > 0 && price >= 0) {
            shoppingList.value = shoppingList.value + ShoppingListItem(
                nextId,
                name,
                quantity,
                price
            )
            nextId++
            newItemName.value = ""
            newItemQuantity.value = ""
            newItemPrice.value = ""
        }
    }

    fun removeItem(itemId: Int) {
        shoppingList.value = shoppingList.value.filterNot { it.id == itemId }
    }

}