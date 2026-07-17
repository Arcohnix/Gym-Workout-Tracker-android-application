package com.example.forgegym.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.data.models.Workout
import com.example.forgegym.ui.components.*
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.HomeUiEvent
import com.example.forgegym.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLibraryClick: () -> Unit,
    onProgressClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onWorkoutClick: (String) -> Unit,
    onContinueClick: (String) -> Unit,
    onAnalyticsClick: () -> Unit,
    onWorkoutBuilderClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    var showGoalDialog by remember { mutableStateOf(false) }

    if (showGoalDialog) {
        WeeklyGoalDialog(
            currentGoal = uiState.weeklyGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { 
                viewModel.onEvent(HomeUiEvent.OnUpdateWeeklyGoal(it))
                showGoalDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = spacing.medium)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(spacing.large))

                GreetingSection(
                    greeting = uiState.greeting,
                    userName = uiState.userName,
                    streakCount = uiState.currentStreak,
                    onSettingsClick = onSettingsClick
                )

                Spacer(modifier = Modifier.height(spacing.large))

                AnimatedVisibility(
                    visible = uiState.activeSession != null,
                    enter = fadeIn() + expandVertically()
                ) {
                    uiState.activeSession?.let { session ->
                        ContinueWorkoutCard(
                            session = session,
                            onContinueClick = onContinueClick
                        )
                        Spacer(modifier = Modifier.height(spacing.large))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    WeeklyGoalProgress(
                        completed = uiState.weeklyWorkoutsCompleted,
                        goal = uiState.weeklyGoal,
                        onClick = { showGoalDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    
                    MotivationCard(
                        quote = uiState.motivationalQuote,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                DashboardStatsGrid(
                    currentStreak = uiState.currentStreak,
                    weeklyWorkouts = uiState.weeklyWorkoutsCompleted,
                    weeklyGoal = uiState.weeklyGoal,
                    weeklyVolume = uiState.weeklyVolume
                )

                Spacer(modifier = Modifier.height(spacing.large))

                uiState.trainingLoad?.let { load ->
                    TrainingLoadCard(metrics = load)
                    Spacer(modifier = Modifier.height(spacing.large))
                }

                SectionTitle(title = "Start Training")
                TodayWorkoutCard(
                    workout = uiState.todayWorkout,
                    onStartWorkout = onWorkoutClick,
                    onViewAllRoutines = onLibraryClick
                )

                if (uiState.allWorkouts.size > 1) {
                    Spacer(modifier = Modifier.height(spacing.medium))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        items(uiState.allWorkouts) { workout ->
                            RoutineSelectionCard(
                                workout = workout,
                                onClick = { onWorkoutClick(workout.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))

                if (uiState.trainingInsights.isNotEmpty()) {
                    SectionTitle(title = "Forge Insights")
                    uiState.trainingInsights.forEach { insight ->
                        TrainingInsightRow(insight)
                        Spacer(modifier = Modifier.height(spacing.small))
                    }
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                SectionTitle(title = "Weekly Activity")
                WeeklyActivityCalendar(workoutDays = uiState.workoutDaysThisWeek)

                Spacer(modifier = Modifier.height(spacing.large))

                uiState.recentSession?.let { session ->
                    SectionTitle(title = "Recent Workout")
                    HistoryWorkoutCard(
                        session = session,
                        onClick = { onContinueClick(session.id) },
                        onDelete = { /* Handled in history */ },
                        onDuplicate = { onWorkoutClick(session.workoutId) }
                    )
                    Spacer(modifier = Modifier.height(spacing.large))
                }

                PersonalRecordsWeekCard(prs = uiState.prsThisWeek)

                Spacer(modifier = Modifier.height(spacing.large))

                if (uiState.workoutSuggestions.isNotEmpty()) {
                    SectionTitle(title = "Suggested for Today")
                    uiState.workoutSuggestions.forEach { suggestion ->
                        RecommendationCard(recommendation = suggestion)
                        Spacer(modifier = Modifier.height(spacing.small))
                    }
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                SectionTitle(title = "Quick Actions")
                Spacer(modifier = Modifier.height(spacing.small))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    QuickActionCard(
                        label = "Builder",
                        icon = Icons.Default.Add,
                        onClick = onWorkoutBuilderClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        label = "Library",
                        icon = Icons.AutoMirrored.Filled.List,
                        onClick = onLibraryClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        label = "History",
                        icon = Icons.Default.History,
                        onClick = onHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(spacing.small))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    QuickActionCard(
                        label = "Analytics",
                        icon = Icons.Default.BarChart,
                        onClick = onAnalyticsClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        label = "Progress",
                        icon = Icons.Default.Timeline,
                        onClick = onProgressClick,
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(spacing.huge))
            }
        }
    }
}

@Composable
fun RoutineSelectionCard(
    workout: Workout,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Text(
                text = "${workout.workoutExercises.size} exercises",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TrainingInsightRow(insight: com.example.forgegym.data.models.TrainingInsight) {
    val spacing = LocalSpacing.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color) = when (insight.type) {
                com.example.forgegym.data.models.InsightType.IMPROVEMENT -> Icons.AutoMirrored.Filled.TrendingUp to Color(0xFF4CAF50)
                com.example.forgegym.data.models.InsightType.PLATEAU -> Icons.Default.Warning to Color(0xFFFF9800)
                com.example.forgegym.data.models.InsightType.DELOAD -> Icons.Default.Info to MaterialTheme.colorScheme.primary
                com.example.forgegym.data.models.InsightType.VOLUME_TREND -> Icons.Default.Analytics to MaterialTheme.colorScheme.secondary
            }
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(spacing.medium))
            Text(text = insight.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun WeeklyGoalProgress(
    completed: Int,
    goal: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val progress = if (goal > 0) completed.toFloat() / goal else 0f
    
    Surface(
        modifier = modifier.clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WEEKLY GOAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(spacing.medium))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text(
                    text = "$completed/$goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun WeeklyGoalDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goal by remember { mutableStateOf(currentGoal.toFloat()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weekly Training Goal") },
        text = {
            Column {
                Text("How many workouts per week?")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = goal,
                    onValueChange = { goal = it },
                    valueRange = 1f..7f,
                    steps = 5
                )
                Text(
                    text = "${goal.toInt()} workouts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(goal.toInt()) }) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
