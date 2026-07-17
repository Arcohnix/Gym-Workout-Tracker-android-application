package com.example.forgegym.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.forgegym.data.models.MuscleGroup
import com.example.forgegym.ui.theme.LocalSpacing
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VolumeChart(
    data: Map<Long, Double>,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val maxVal = (data.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val sortedData = data.toList().sortedBy { it.first }
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val barCount = data.size.coerceAtLeast(1)
            val barWidth = size.width / (barCount * 1.5f)
            val gap = (size.width - (barWidth * barCount)) / (barCount + 1)

            sortedData.forEachIndexed { index, pair ->
                val value = pair.second
                val barHeight = (value / maxVal) * size.height
                
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(
                        x = gap + index * (barWidth + gap),
                        y = (size.height - barHeight).toFloat()
                    ),
                    size = Size(barWidth, barHeight.toFloat()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
        
        Spacer(modifier = Modifier.height(spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (sortedData.isNotEmpty()) {
                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                Text(text = sdf.format(Date(sortedData.first().first)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (sortedData.size > 2) {
                    Text(text = sdf.format(Date(sortedData[sortedData.size / 2].first)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = sdf.format(Date(sortedData.last().first)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DonutChart(
    data: Map<MuscleGroup, Double>,
    modifier: Modifier = Modifier
) {
    val totalVolume = data.values.sum().coerceAtLeast(1.0)
    val sortedData = data.toList().sortedByDescending { it.second }.take(6)
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                sortedData.forEachIndexed { index, pair ->
                    val sweepAngle = (pair.second / totalVolume * 360).toFloat()
                    drawArc(
                        color = colors.getOrElse(index) { Color.Gray },
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "TOTAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${(totalVolume / 1000).toInt()}k", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            sortedData.forEachIndexed { index, pair ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(colors.getOrElse(index) { Color.Gray }, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = pair.first.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.weight(1f))
                    val percent = (pair.second / totalVolume * 100).toInt()
                    Text(text = "$percent%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContributionHeatmap(
    data: Map<Long, Int>,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.add(Calendar.DAY_OF_YEAR, -364)
    val days = (0..364).map {
        val time = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        time
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Last 365 Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(spacing.small))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            maxItemsInEachRow = 53 // Approx weeks in a year
        ) {
            days.forEach { timestamp ->
                val intensity = data[timestamp] ?: 0
                val color = when {
                    intensity >= 3 -> MaterialTheme.colorScheme.primary
                    intensity == 2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    intensity == 1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, MaterialTheme.shapes.extraSmall)
                )
            }
        }
    }
}
