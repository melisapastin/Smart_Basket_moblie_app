package com.example.smartbasket

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(
    onBasketClick: () -> Unit,
    onTrendsClick: () -> Unit,
    onListClick: () -> Unit,
    onWalletClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onBasketClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("My Basket", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onTrendsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Shopping Trends", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onListClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Shopping List", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onWalletClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ){
            Text("My Wallet", fontSize = 20.sp)
        }
    }
}