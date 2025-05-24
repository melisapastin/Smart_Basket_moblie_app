
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
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

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

    var cloudProducts by mutableStateOf<List<Product>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    init {
        fetchCloudProducts()
    }

    private fun fetchCloudProducts() {
        viewModelScope.launch {
            try {
                isLoading = true
                val products = withContext(Dispatchers.IO) {
                    val url = URL("https://smartcart1.blob.core.windows.net/products/products.json?sp=r&st=2025-05-07T11:22:29Z&se=2025-06-30T19:22:29Z&spr=https&sv=2024-11-04&sr=b&sig=SzDu6sbxIhfVECYPSjMdjszW0c%2F6zeJUl0aVkoykr%2Fo%3D\n")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.apply {
                        requestMethod = "GET"
                        connectTimeout = 5000
                        readTimeout = 5000
                    }

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        // Fixed: Use connection.inputStream
                        val json = connection.inputStream.bufferedReader().use { it.readText() }
                        Json.decodeFromString<List<Product>>(json)
                    } else emptyList()
                }
                cloudProducts = products
            } catch (e: Exception) {
                error = "Failed to load products: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun addCloudProduct(product: Product, quantity: Int) {
        val newItem = ShoppingListItem(
            id = nextId,
            name = product.name,
            quantity = quantity,
            price = product.price
        )
        shoppingList.value = shoppingList.value + newItem
        nextId++
        saveList(shoppingList.value)
    }

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