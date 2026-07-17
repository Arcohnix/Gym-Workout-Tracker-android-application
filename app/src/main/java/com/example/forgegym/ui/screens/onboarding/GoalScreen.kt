package com.example.forgegym.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.data.models.TrainingGoal
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.OnboardingUiEvent
import com.example.forgegym.viewmodel.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current

    LaunchedEffect(uiState.isOnboardingCompleted) {
        if (uiState.isOnboardingCompleted) {
            onComplete()
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { viewModel.onEvent(OnboardingUiEvent.OnCompleteOnboarding) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.medium)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("LET'S GO", fontWeight = FontWeight.ExtraBold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(spacing.medium)
        ) {
            Spacer(modifier = Modifier.height(spacing.large))
            Text(
                text = "What's your primary goal?",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "We will tailor your recommendations accordingly.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(spacing.extraLarge))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                items(TrainingGoal.entries) { goal ->
                    GoalCard(
                        goal = goal,
                        isSelected = uiState.selectedGoal == goal,
                        onClick = { viewModel.onEvent(OnboardingUiEvent.OnGoalSelected(goal)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: TrainingGoal,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val title = goal.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    val description = when (goal) {
        TrainingGoal.WEIGHT_LOSS -> "Focus on burning fat and maintaining lean mass."
        TrainingGoal.BUILD_MUSCLE -> "Maximize hypertrophy and size gains."
        TrainingGoal.FITNESS -> "General health, mobility, and steady progress."
        TrainingGoal.STRENGTH -> "Priority on absolute power and low-rep compound sets."
        TrainingGoal.ENDURANCE -> "Higher rep ranges and conditioning work."
    }

    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(Icons.Default.Done, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
