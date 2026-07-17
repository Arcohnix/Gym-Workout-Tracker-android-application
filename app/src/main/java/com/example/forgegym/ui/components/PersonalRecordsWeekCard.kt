package com.example.forgegym.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forgegym.data.models.PRType
import com.example.forgegym.data.models.PersonalRecord
import com.example.forgegym.ui.theme.LocalSpacing

@Composable
fun PersonalRecordsWeekCard(
    prs: List<PersonalRecord>,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    if (prs.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(title = "PRs This Week 🏆")
        
        Spacer(modifier = Modifier.height(spacing.small))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(spacing.medium)) {
                prs.take(3).forEachIndexed { index, pr ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = pr.exerciseName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pr.type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        val value = when (pr.type) {
                            PRType.MAX_WEIGHT -> "${pr.weight} kg"
                            PRType.MAX_VOLUME -> "${pr.volume.toInt()} kg"
                            PRType.MAX_REPS -> "${pr.reps} reps"
                            PRType.ESTIMATED_1RM -> "${pr.estimatedOneRepMax.toInt()} kg"
                        }
                        
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    if (index < prs.size - 1 && index < 2) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = spacing.small),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
