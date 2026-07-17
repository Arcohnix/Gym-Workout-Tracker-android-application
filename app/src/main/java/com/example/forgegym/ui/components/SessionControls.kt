package com.example.forgegym.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.forgegym.ui.theme.LocalSpacing

@Composable
fun SessionControls(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isFirstExercise: Boolean,
    isLastExercise: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        OutlinedButton(
            onClick = onPrevious,
            modifier = Modifier.weight(1f),
            enabled = !isFirstExercise
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.weight(1f)
        ) {
            Text("SKIP")
        }

        FilledTonalButton(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = !isLastExercise
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
