package com.example.forgegym.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forgegym.data.models.RecoveryStatus
import com.example.forgegym.data.models.TrainingLoadMetrics
import com.example.forgegym.ui.theme.LocalSpacing

@Composable
fun TrainingLoadCard(
    metrics: TrainingLoadMetrics,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val statusColor = when (metrics.recoveryStatus) {
        RecoveryStatus.LOW -> MaterialTheme.colorScheme.error
        RecoveryStatus.MODERATE -> Color(0xFFFF9800)
        RecoveryStatus.HIGH -> Color(0xFF4CAF50)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(spacing.small))
                    Text(
                        text = "TRAINING LOAD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = metrics.recoveryStatus.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LoadItem(label = "Stress Ratio", value = String.format("%.2f", metrics.stressRatio), color = statusColor)
                LoadItem(label = "Fatigue", value = "${metrics.fatigueScore}%", color = statusColor)
                LoadItem(label = "Acute", value = "${(metrics.acuteLoad / 1000).toInt()}k", color = MaterialTheme.colorScheme.onSurface)
            }
            
            Spacer(modifier = Modifier.height(spacing.small))
            
            LinearProgressIndicator(
                progress = { metrics.fatigueScore / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
private fun LoadItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = color)
    }
}
