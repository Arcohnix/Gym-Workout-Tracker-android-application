package com.example.forgegym.ui.screens.session

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.data.models.CompletedSet
import com.example.forgegym.data.models.ExerciseSession
import com.example.forgegym.ui.components.*
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.WorkoutSessionUiEvent
import com.example.forgegym.viewmodel.WorkoutSessionViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    onFinishSession: (String) -> Unit,
    onDiscardSession: () -> Unit,
    viewModel: WorkoutSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onFinishSession(uiState.finishedSessionId ?: "")
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Workout?") },
            text = { Text("Are you sure you want to discard this workout? All progress will be lost.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.onEvent(WorkoutSessionUiEvent.OnDiscardSession)
                    onDiscardSession()
                }) {
                    Text("DISCARD", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = uiState.session?.workoutName ?: "Workout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.session?.let { session ->
                            Text(
                                text = if (session.isPaused) "Paused" else formatElapsedTime(uiState.elapsedTimeSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (session.isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(WorkoutSessionUiEvent.OnTogglePauseSession) }) {
                        Icon(
                            imageVector = if (uiState.session?.isPaused == true) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause/Resume"
                        )
                    }
                    IconButton(onClick = { showDiscardDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Discard")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                RestTimerBar(
                    remainingSeconds = uiState.restTimeRemaining,
                    isVisible = uiState.isResting,
                    isPaused = uiState.isTimerPaused,
                    onTogglePause = { viewModel.onEvent(WorkoutSessionUiEvent.OnToggleTimer) },
                    onSkip = { viewModel.onEvent(WorkoutSessionUiEvent.OnSkipTimer) }
                )
                Box(modifier = Modifier.padding(spacing.medium)) {
                    Button(
                        onClick = { viewModel.onEvent(WorkoutSessionUiEvent.OnFinishSession) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        enabled = uiState.session?.isPaused == false
                    ) {
                        Text(text = "FINISH WORKOUT", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    ) { innerPadding ->
        val session = uiState.session
        if (uiState.isLoading || (session == null && !uiState.isFinished)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (session != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = spacing.large)
                ) {
                    item {
                        LinearProgressIndicator(
                            progress = { 
                                val completedCount = session.exercises.count { it.sets.isNotEmpty() }
                                if (session.exercises.isEmpty()) 0f else completedCount.toFloat() / session.exercises.size
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    itemsIndexed(session.exercises, key = { _, it -> it.exerciseId }) { index, exerciseSession ->
                        val isSelected = index == session.currentExerciseIndex
                        
                        ExerciseSessionItem(
                            exerciseSession = exerciseSession,
                            isSelected = isSelected,
                            lastPerformance = if (isSelected) uiState.lastPerformance else emptyList(),
                            recommendation = if (isSelected) uiState.recommendation else null,
                            weightInput = uiState.weightInput,
                            repsInput = uiState.repsInput,
                            noteInput = uiState.noteInput,
                            onWeightChange = { viewModel.onEvent(WorkoutSessionUiEvent.OnWeightChanged(it)) },
                            onRepsChange = { viewModel.onEvent(WorkoutSessionUiEvent.OnRepsChanged(it)) },
                            onNoteChange = { viewModel.onEvent(WorkoutSessionUiEvent.OnNoteChanged(it)) },
                            onCompleteSet = { viewModel.onEvent(WorkoutSessionUiEvent.OnCompleteSet) },
                            onDeleteSet = { viewModel.onEvent(WorkoutSessionUiEvent.OnDeleteSet(it)) },
                            onClick = { 
                                if (!isSelected) {
                                    viewModel.onEvent(WorkoutSessionUiEvent.OnSelectExercise(index))
                                }
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(spacing.huge)) }
                }

                if (session.isPaused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                            .clickable { viewModel.onEvent(WorkoutSessionUiEvent.OnTogglePauseSession) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "WORKOUT PAUSED",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Tap to Resume",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseSessionItem(
    exerciseSession: ExerciseSession,
    isSelected: Boolean,
    lastPerformance: List<CompletedSet>,
    recommendation: com.example.forgegym.data.models.ExerciseRecommendation?,
    weightInput: String,
    repsInput: String,
    noteInput: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCompleteSet: () -> Unit,
    onDeleteSet: (String) -> Unit,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val isDone = exerciseSession.sets.isNotEmpty()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium, vertical = spacing.small)
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.large,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (isDone) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Done",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(spacing.small))
                    }
                    Text(
                        text = exerciseSession.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Icon(
                    imageVector = if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(visible = isSelected) {
                Column {
                    Spacer(modifier = Modifier.height(spacing.medium))

                    recommendation?.let {
                        RecommendationCard(recommendation = it)
                        Spacer(modifier = Modifier.height(spacing.medium))
                    }

                    ComparisonSection(lastPerformance = lastPerformance)

                    Spacer(modifier = Modifier.height(spacing.large))

                    SetLoggerSection(
                        weight = weightInput,
                        reps = repsInput,
                        note = noteInput,
                        onWeightChange = onWeightChange,
                        onRepsChange = onRepsChange,
                        onNoteChange = onNoteChange,
                        onCompleteSet = onCompleteSet
                    )

                    if (exerciseSession.sets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(spacing.large))
                        Text(
                            text = "Completed Sets",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(spacing.small))
                        SessionSetsTable(
                            sets = exerciseSession.sets,
                            onDeleteSet = onDeleteSet
                        )
                    }
                }
            }
            
            if (!isSelected && isDone) {
                Text(
                    text = "${exerciseSession.sets.size} sets completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun formatElapsedTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }
}

@Composable
fun ComparisonSection(lastPerformance: List<CompletedSet>) {
    val spacing = LocalSpacing.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Text(
                text = "PREVIOUS BEST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            if (lastPerformance.isEmpty()) {
                Text(
                    text = "No previous data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                lastPerformance.forEach { set ->
                    Text(
                        text = "${set.weight} kg × ${set.reps}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
