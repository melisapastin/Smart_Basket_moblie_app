package com.example.smartbasket

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.min

// Add this new ViewModel for shopping trends
class ShoppingTrendsViewModel : ViewModel() {
    // Mock purchase history data
    private val mockPurchaseHistory = listOf(
        BasketItem("Coca-Cola Can", 1.5, 6, "Drink"),
        BasketItem("Lay's Classic Chips", 2.0, 3, "Snack"),
        BasketItem("Fares Tea - Merisoare si Afine", 1.2, 3, "Tea"),
        BasketItem("Snickers Chocolate Bar", 1.0, 2, "Snack"),
        BasketItem("Red Apple", 0.8, 10, "Fruit"),
        BasketItem("Zuzu Milk Carton", 1.8, 2, "Dairy"),
        BasketItem("Coca-Cola Can", 1.5, 5, "Drink"),
        BasketItem("Lay's Classic Chips", 2.0, 2, "Snack"),
        BasketItem("Fares Tea - Merisoare si Afine", 1.2, 4, "Tea"),
        BasketItem("Snickers Chocolate Bar", 1.0, 3, "Snack"),
        BasketItem("Red Apple", 0.8, 14, "Fruit"),
        BasketItem("Zuzu Milk Carton", 1.8, 4, "Dairy")
    )

    fun getTopItemsByQuantity(): List<Pair<String, Int>> {
        return mockPurchaseHistory.groupBy { it.name }
            .mapValues { (_, items) -> items.sumOf { it.quantity } }
            .entries.sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }
    }

    fun getTopItemsBySales(): List<Pair<String, Double>> {
        return mockPurchaseHistory.groupBy { it.name }
            .mapValues { (_, items) -> items.sumOf { it.price * it.quantity } }
            .entries.sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }
    }

    fun getCategoryDistribution(): Map<String, Double> {
        return mockPurchaseHistory.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.price * it.quantity } }
    }
}

// Update the ShoppingTrendsScreen composable
@Composable
fun ShoppingTrendsScreen(
    onBack: () -> Unit,
    trendsViewModel: ShoppingTrendsViewModel = viewModel() // Use the new ViewModel
) {
    val topByQuantity = trendsViewModel.getTopItemsByQuantity()
    val topBySales = trendsViewModel.getTopItemsBySales()
    val categoryDistribution = trendsViewModel.getCategoryDistribution()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Back to Menu", color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rest of the screen remains the same, using the mock data from trendsViewModel
        Text(
            text = "Top Items by Quantity",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        VerticalBarChart(
            data = topByQuantity,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sales by Category",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        PieChart(
            data = categoryDistribution.values.toList(),
            labels = categoryDistribution.keys.toList(),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Top Items by Sales",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalBarChart(
            data = topBySales,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

// Vertical Bar Chart Component
@Composable
fun VerticalBarChart(data: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    val maxValue = data.maxOfOrNull { it.second }?.toFloat() ?: 1f
    val colors = listOf(Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFF9800), Color(0xFFE91E63))

    Box(
        modifier = modifier.padding(vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, (label, value) ->
                val heightRatio = value.toFloat() / maxValue
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((150.dp * heightRatio).coerceAtLeast(8.dp))
                            .background(
                                color = colors[index % colors.size],
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// Pie Chart Component
// Pie Chart Component (Updated to fix color matching)
@Composable
fun PieChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val total = data.sum()
    if (total <= 0) return  // Handle empty data case

    // Create pairs of labels and values
    val categoryData = labels.zip(data).map { (label, value) ->
        Pair(label, value)
    }

    // Sort by percentage descending (largest first)
    val sortedCategoryData = categoryData.sortedByDescending { it.second }

    val colors = listOf(Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFFFFC107))
    var startAngle = -90f  // Start at 12 o'clock

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val diameter = min(size.width, size.height)
                val canvasSize = Size(diameter, diameter)
                val offset = Offset(
                    (size.width - diameter) / 2,
                    (size.height - diameter) / 2
                )

                // Draw slices in sorted order
                sortedCategoryData.forEachIndexed { index, (_, value) ->
                    val sweepAngle = (value / total * 360).toFloat()
                    drawPieSlice(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        size = canvasSize,
                        offset = offset
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            // Legend uses same sorted order
            sortedCategoryData.forEachIndexed { index, (label, value) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index % colors.size])
                    )
                    Text(
                        text = "$label (${"%.1f".format(value/total*100)}%)",
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawPieSlice(
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
    size: Size,
    offset: Offset
) {
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        size = size,
        topLeft = offset
    )
}

// Horizontal Bar Chart Component
@Composable
fun HorizontalBarChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1.0
    val colors = listOf(Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFF9800), Color(0xFFE91E63))

    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEachIndexed { index, (label, value) ->
            val widthPercentage = (value / maxValue).coerceIn(0.0, 1.0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed-width label
                Text(
                    text = label,
                    modifier = Modifier.width(90.dp),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Bar area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.LightGray, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = widthPercentage.toFloat())
                            .fillMaxHeight()
                            .background(
                                color = colors[index % colors.size],
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Fixed-width value text
                Text(
                    text = "€${"%.2f".format(value)}",
                    modifier = Modifier.width(60.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
