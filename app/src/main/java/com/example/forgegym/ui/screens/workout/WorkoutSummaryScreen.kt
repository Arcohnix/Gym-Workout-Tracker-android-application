package com.example.forgegym.ui.screens.workout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.data.models.WorkoutComparison
import com.example.forgegym.ui.components.PersonalRecordItem
import com.example.forgegym.ui.components.StatsCard
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.WorkoutSummaryUiEvent
import com.example.forgegym.viewmodel.WorkoutSummaryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    sessionId: String,
    onFinish: () -> Unit,
    onRepeat: (String) -> Unit,
    viewModel: WorkoutSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current

    LaunchedEffect(uiState.isRepeating) {
        if (uiState.isRepeating) {
            onRepeat(uiState.summary?.workoutId ?: "")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(spacing.medium)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(WorkoutSummaryUiEvent.OnRepeatWorkout) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(spacing.small))
                        Text("REPEAT", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(spacing.small))
                        Text("FINISH", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            val summary = uiState.summary!!
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = spacing.medium)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(spacing.large))
                
                Text(
                    text = "Workout Summary",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = summary.workoutName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(spacing.extraLarge))

                summary.comparison?.let { 
                    ComparisonCard(it)
                    Spacer(modifier = Modifier.height(spacing.large))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    StatsCard(
                        label = "Duration",
                        value = "${summary.durationMinutes}",
                        subLabel = "Minutes",
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        label = "Volume",
                        value = "${summary.totalVolume.toInt()}",
                        subLabel = "kg lifted",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    StatsCard(
                        label = "Sets",
                        value = "${summary.totalSets}",
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        label = "Exercises",
                        value = "${summary.exercisesCompleted}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // Detailed Log
                Text(
                    text = "Workout Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(spacing.small))

                val loggedExercises = summary.exercises.filter { it.sets.isNotEmpty() }
                if (loggedExercises.isEmpty()) {
                    Text(
                        text = "No sets were logged during this session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    loggedExercises.forEach { exerciseSession ->
                        ExerciseLogItem(exerciseSession)
                        Spacer(modifier = Modifier.height(spacing.small))
                    }
                }

                if (summary.newPersonalRecords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(spacing.large))
                    
                    Text(
                        text = "New Personal Records 🏆",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(spacing.small))
                    
                    summary.newPersonalRecords.forEach { pr ->
                        PersonalRecordItem(pr = pr)
                        Spacer(modifier = Modifier.height(spacing.small))
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))

                OutlinedButton(
                    onClick = { /* Share logic */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(spacing.small))
                    Text("SHARE PERFORMANCE")
                }

                Spacer(modifier = Modifier.height(spacing.huge))
            }
        }
    }
}

@Composable
fun ExerciseLogItem(exerciseSession: com.example.forgegym.data.models.ExerciseSession) {
    val spacing = LocalSpacing.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Text(
                text = exerciseSession.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(spacing.small))
            exerciseSession.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Set ${index + 1}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${set.weight} kg × ${set.reps}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                if (set.note.isNotEmpty()) {
                    Text(
                        text = "Note: ${set.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ComparisonCard(comparison: WorkoutComparison) {
    val spacing = LocalSpacing.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Text(
                text = "VS PREVIOUS SESSION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(spacing.small))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ComparisonItem(
                    label = "Volume",
                    diff = comparison.volumeDifference,
                    unit = "kg",
                    modifier = Modifier.weight(1f)
                )
                ComparisonItem(
                    label = "Duration",
                    diff = comparison.durationDifferenceMinutes.toDouble(),
                    unit = "m",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ComparisonItem(
    label: String,
    diff: Double,
    unit: String,
    modifier: Modifier = Modifier
) {
    val isPositive = diff >= 0
    val color = if (isPositive) MaterialTheme.colorScheme.primary else Color.Gray
    val icon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
    val sign = if (isPositive) "+" else ""

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$sign${diff.toInt()}$unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
