package com.example.forgegym.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.ui.components.EmptyState
import com.example.forgegym.ui.components.HistoryCalendar
import com.example.forgegym.ui.components.HistoryWorkoutCard
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.HistoryUiEvent
import com.example.forgegym.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSessionClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = { Text("Training Journal", fontWeight = FontWeight.Black) },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters")
                        }
                    }
                )
                
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onEvent(HistoryUiEvent.OnSearchQueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.medium),
                    placeholder = { Text("Search exercises or routines...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(spacing.small))

                if (showFilters) {
                    HistoryCalendar(
                        selectedDate = uiState.selectedDate,
                        onDateSelected = { viewModel.onEvent(HistoryUiEvent.OnDateSelected(it)) }
                    )
                    Spacer(modifier = Modifier.height(spacing.small))
                }
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
                if (uiState.sessions.isEmpty()) {
                    item {
                        EmptyState(
                            message = if (uiState.searchQuery.isEmpty()) "Your training legacy starts here.\nComplete a workout to see it in your journal." else "No sessions found matching your search.",
                            icon = Icons.Default.DateRange
                        )
                    }
                } else {
                    items(uiState.sessions, key = { it.id }) { session ->
                        HistoryWorkoutCard(
                            session = session,
                            onClick = { onSessionClick(session.id) },
                            onDelete = { viewModel.onEvent(HistoryUiEvent.OnDeleteSession(session.id)) },
                            onDuplicate = { viewModel.onEvent(HistoryUiEvent.OnDuplicateSession(session.id)) }
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
