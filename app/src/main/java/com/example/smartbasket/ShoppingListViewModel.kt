
package com.example.smartbasket

// ShoppingListViewModel.kt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ShoppingListItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val price: Double
)

// ShoppingListViewModel.kt
class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("shopping_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    val shoppingList = mutableStateOf(loadSavedList())
    val newItemName = mutableStateOf("")
    val newItemQuantity = mutableStateOf("")
    private var nextId by mutableStateOf(loadNextId())
    val newItemPrice = mutableStateOf("")

    fun addItem() {
        val name = newItemName.value.trim()
        val quantity = newItemQuantity.value.toIntOrNull() ?: 0
        val price = newItemPrice.value.toDoubleOrNull() ?: 0.0

        if (name.isNotBlank() && quantity > 0 && price >= 0) {
            val newList = shoppingList.value + ShoppingListItem(
                nextId,
                name,
                quantity,
                price
            )
            shoppingList.value = newList
            nextId++
            saveList(newList)
            resetFields()
        }
    }

    fun removeItem(itemId: Int) {
        val newList = shoppingList.value.filterNot { it.id == itemId }
        shoppingList.value = newList
        saveList(newList)
    }

    private fun saveList(list: List<ShoppingListItem>) {
        val json = gson.toJson(list)
        sharedPrefs.edit()
            .putString("shopping_list", json)
            .putInt("next_id", nextId)
            .apply()
    }

    private fun loadSavedList(): List<ShoppingListItem> {
        val json = sharedPrefs.getString("shopping_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<ShoppingListItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun loadNextId(): Int {
        return sharedPrefs.getInt("next_id", 0)
    }

    private fun resetFields() {
        newItemName.value = ""
        newItemQuantity.value = ""
        newItemPrice.value = ""
    }
}

// Add this factory class
class ShoppingListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShoppingListViewModel(application) as T
    }
}