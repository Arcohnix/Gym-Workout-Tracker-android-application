package com.example.forgegym.ui.screens.library

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.data.models.*
import com.example.forgegym.ui.components.EmptyState
import com.example.forgegym.ui.components.ExerciseListItem
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.ExerciseLibraryUiEvent
import com.example.forgegym.viewmodel.ExerciseLibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onExerciseClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ExerciseLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = { Text("Exercise Library", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onEvent(ExerciseLibraryUiEvent.OnToggleFavoritesOnly) }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorites",
                                tint = if (uiState.showOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                )
                
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onEvent(ExerciseLibraryUiEvent.OnSearchQueryChanged(it)) },
                    modifier = Modifier.padding(horizontal = spacing.medium)
                )

                AnimatedVisibility(visible = showFilters) {
                    FilterSection(
                        selectedMuscle = uiState.selectedMuscleGroup,
                        onMuscleSelected = { viewModel.onEvent(ExerciseLibraryUiEvent.OnMuscleGroupSelected(it)) },
                        selectedEquipment = uiState.selectedEquipment,
                        onEquipmentSelected = { viewModel.onEvent(ExerciseLibraryUiEvent.OnEquipmentSelected(it)) },
                        selectedDifficulty = uiState.selectedDifficulty,
                        onDifficultySelected = { viewModel.onEvent(ExerciseLibraryUiEvent.OnDifficultySelected(it)) }
                    )
                }
                
                Spacer(modifier = Modifier.height(spacing.small))
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
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                if (uiState.recentlyUsed.isNotEmpty() && uiState.searchQuery.isEmpty()) {
                    item {
                        Text(
                            text = "Recently Used",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = spacing.small)
                        )
                    }
                    items(uiState.recentlyUsed, key = { "recent_${it.id}" }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onToggleFavorite = { viewModel.onEvent(ExerciseLibraryUiEvent.OnToggleFavorite(exercise.id, it)) },
                            onClick = { onExerciseClick(exercise.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(spacing.medium)) }
                }

                if (uiState.filteredExercises.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No exercises found matching your filters.",
                            icon = Icons.Default.Search
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "All Exercises",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = spacing.small)
                        )
                    }
                    items(uiState.filteredExercises, key = { it.id }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onToggleFavorite = { viewModel.onEvent(ExerciseLibraryUiEvent.OnToggleFavorite(exercise.id, it)) },
                            onClick = { onExerciseClick(exercise.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.huge))
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search 400+ exercises...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        singleLine = true
    )
}

@Composable
fun FilterSection(
    selectedMuscle: MuscleGroup,
    onMuscleSelected: (MuscleGroup) -> Unit,
    selectedEquipment: Equipment,
    onEquipmentSelected: (Equipment) -> Unit,
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit
) {
    val spacing = LocalSpacing.current
    
    Column(modifier = Modifier.padding(vertical = spacing.small)) {
        FilterRow(
            label = "Muscle Group",
            items = MuscleGroup.entries,
            selectedItem = selectedMuscle,
            onItemSelected = onMuscleSelected
        )

        FilterRow(
            label = "Equipment",
            items = Equipment.entries.filter { it != Equipment.ALL },
            selectedItem = selectedEquipment,
            onItemSelected = onEquipmentSelected
        )

        FilterRow(
            label = "Difficulty",
            items = Difficulty.entries,
            selectedItem = selectedDifficulty,
            onItemSelected = onDifficultySelected
        )
    }
}

@Composable
fun <T> FilterRow(
    label: String,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit
) {
    val spacing = LocalSpacing.current
    
    Column(modifier = Modifier.padding(top = spacing.small)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = spacing.medium)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
        ) {
            items(items) { item ->
                FilterChip(
                    selected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    label = { Text(item.toString().lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedItem == item,
                        borderColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}
