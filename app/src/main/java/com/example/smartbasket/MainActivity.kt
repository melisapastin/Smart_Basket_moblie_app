package com.example.smartbasket
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartbasket.MenuScreen
import com.example.smartbasket.ReceiptScreen
import com.example.smartbasket.ui.theme.SmartBasketTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import android.content.Context
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartBasketTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val (currentScreen, setCurrentScreen) = remember { mutableStateOf<Screen>(Screen.Menu) }
    val shoppingListViewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModelFactory(context.applicationContext as Application)
    )
    val receiptViewModel: ReceiptViewModel = viewModel()
    val walletViewModel: WalletViewModel = viewModel(
        factory = WalletViewModelFactory(context.applicationContext as Application)
    )

    when (currentScreen) {
        Screen.Menu -> MenuScreen(
            onBasketClick = { setCurrentScreen(Screen.Receipt) },
            onTrendsClick = { setCurrentScreen(Screen.ShoppingTrends) },
            onListClick = { setCurrentScreen(Screen.ShoppingList) },
            onWalletClick = { setCurrentScreen(Screen.MyWallet) }
        )
        Screen.Receipt -> ReceiptScreen(
            onBack = { setCurrentScreen(Screen.Menu) },
            shoppingListViewModel = shoppingListViewModel
        )
        Screen.ShoppingList -> ShoppingListScreen(
            shoppingListViewModel = shoppingListViewModel,  // This is correct
            onBack = { setCurrentScreen(Screen.Menu) }
        )
        Screen.ShoppingTrends -> ShoppingTrendsScreen(
            onBack = { setCurrentScreen(Screen.Menu) }
        )
        Screen.MyWallet -> WalletScreen(
            onBack = { setCurrentScreen(Screen.Menu) },
        )
    }
}

sealed class Screen {
    object Menu : Screen()
    object Receipt : Screen()
    object ShoppingList : Screen()
    object ShoppingTrends : Screen()
    object MyWallet : Screen()  // Make sure this is properly defined
}

@Preview(showBackground = true)
@Composable
fun PreviewReceiptScreen() {
    SmartBasketTheme {
        val context = LocalContext.current
        val shoppingListViewModel: ShoppingListViewModel = viewModel(
            factory = ShoppingListViewModelFactory(context.applicationContext as Application)
        )
        ReceiptScreen(
            onBack = {},
            shoppingListViewModel = shoppingListViewModel
        )
    }
}