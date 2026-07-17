package com.example.forgegym.ui.screens.workout

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.ui.components.EditableWorkoutExerciseItem
import com.example.forgegym.ui.components.ExerciseSelectorModal
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.WorkoutBuilderUiEvent
import com.example.forgegym.viewmodel.WorkoutBuilderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutBuilderScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    onViewExerciseDetails: (String) -> Unit,
    viewModel: WorkoutBuilderViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    var showExerciseSelector by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onSaveSuccess()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.isLoading) "Loading..." else uiState.name.ifBlank { "New Workout" },
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = { 
                        if (uiState.name.isBlank()) {
                            android.widget.Toast.makeText(context, "Please enter a workout name", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.onEvent(WorkoutBuilderUiEvent.OnSaveWorkout)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                    if (uiState.isEditMode) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete Workout", color = MaterialTheme.colorScheme.error) },
                                    onClick = { 
                                        showMenu = false
                                        viewModel.onEvent(WorkoutBuilderUiEvent.OnDeleteWorkout)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showExerciseSelector = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                item {
                    Column {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onEvent(WorkoutBuilderUiEvent.OnNameChanged(it)) },
                            label = { Text("Workout Name") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Push Day, Leg Power") },
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(spacing.small))
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.onEvent(WorkoutBuilderUiEvent.OnDescriptionChanged(it)) },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                item {
                    Text(
                        text = "Exercises (${uiState.exercises.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(vertical = spacing.small)
                    )
                }

                if (uiState.exercises.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No exercises added yet.\nTap + to build your routine.",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(uiState.exercises, key = { _, item -> item.id }) { index, workoutExercise ->
                        EditableWorkoutExerciseItem(
                            workoutExercise = workoutExercise,
                            onRemove = { viewModel.onEvent(WorkoutBuilderUiEvent.OnRemoveExercise(workoutExercise.id)) },
                            onRestTimeChange = { viewModel.onEvent(WorkoutBuilderUiEvent.OnRestTimeChanged(workoutExercise.id, it)) },
                            onNoteChange = { viewModel.onEvent(WorkoutBuilderUiEvent.OnNoteChanged(workoutExercise.id, it)) },
                            onMoveUp = { viewModel.onEvent(WorkoutBuilderUiEvent.OnReorderExercises(index, index - 1)) },
                            onMoveDown = { viewModel.onEvent(WorkoutBuilderUiEvent.OnReorderExercises(index, index + 1)) },
                            isFirst = index == 0,
                            isLast = index == uiState.exercises.size - 1
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(spacing.huge))
                }
            }
        }
    }

    if (showExerciseSelector) {
        ExerciseSelectorModal(
            exercises = uiState.filteredAvailableExercises,
            searchQuery = uiState.exerciseSearchQuery,
            onSearchQueryChange = { viewModel.onEvent(WorkoutBuilderUiEvent.OnExerciseSearchQueryChanged(it)) },
            selectedMuscle = uiState.selectedMuscleGroup,
            onMuscleSelected = { viewModel.onEvent(WorkoutBuilderUiEvent.OnMuscleGroupSelected(it)) },
            onExerciseSelected = {
                viewModel.onEvent(WorkoutBuilderUiEvent.OnAddExercise(it))
                showExerciseSelector = false
            },
            onViewDetails = onViewExerciseDetails,
            onToggleFavorite = { id, fav -> viewModel.onEvent(WorkoutBuilderUiEvent.OnToggleFavoriteExercise(id, fav)) },
            onDismiss = { showExerciseSelector = false }
        )
    }
}
