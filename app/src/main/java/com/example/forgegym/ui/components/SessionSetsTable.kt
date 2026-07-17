package com.example.forgegym.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forgegym.data.models.CompletedSet
import com.example.forgegym.ui.theme.LocalSpacing

@Composable
fun SessionSetsTable(
    sets: List<CompletedSet>,
    onDeleteSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "SET", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.5f))
            Text(text = "WEIGHT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.5f))
            Text(text = "REPS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            Text(text = "NOTE", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(2f))
            Text(text = "ACTION", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        }

        sets.forEachIndexed { index, set ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.5f)
                )
                Text(
                    text = "${set.weight} kg",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "${set.reps}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = set.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(2f),
                    maxLines = 1
                )
                IconButton(
                    onClick = { onDeleteSet(set.id) },
                    modifier = Modifier.weight(1f).size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Set",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
