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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.min

// Shopping Trends Screen
@Composable
fun ShoppingTrendsScreen(
    onBack: () -> Unit,
    receiptViewModel: ReceiptViewModel = viewModel()
) {
    val topByQuantity = receiptViewModel.getTopItemsByQuantity()
    val topBySales = receiptViewModel.getTopItemsBySales()
    val categoryDistribution = receiptViewModel.getCategoryDistribution()

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

        // 1. Vertical Bar Chart - Most Purchased Items by Quantity
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

        // 2. Pie Chart - Sales by Category
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

        // 3. Horizontal Bar Chart - Top Items by Sales
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
// Pie Chart Component
@Composable
fun PieChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val total = data.sum()
    if (total <= 0) return  // Handle empty data case

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

                data.forEachIndexed { index, value ->
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
            labels.forEachIndexed { index, label ->
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
                        text = "$label (${"%.1f".format(data[index]/total*100)}%)",
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                // Calculate width percentage relative to max value
                val widthPercentage = (value / maxValue).coerceIn(0.0, 1.0)

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.width(100.dp),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Bar background
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.LightGray, RoundedCornerShape(4.dp))
                    ) {
                        // Actual colored bar
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

                    Text(
                        text = "€${"%.2f".format(value)}",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}