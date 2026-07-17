package com.example.forgegym.ui.screens.progress

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.data.models.Exercise
import com.example.forgegym.data.models.PersonalRecord
import com.example.forgegym.data.repository.Achievement
import com.example.forgegym.data.repository.TimeRange
import com.example.forgegym.ui.components.*
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.ProgressUiEvent
import com.example.forgegym.viewmodel.ProgressViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Performance & Analytics", fontWeight = FontWeight.Black) }
            )
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
                verticalArrangement = Arrangement.spacedBy(spacing.large)
            ) {
                item {
                    PerformanceScoreCard(uiState.performanceScore)
                }

                item {
                    Column {
                        SectionTitle(title = "Workout Frequency")
                        Spacer(modifier = Modifier.height(spacing.small))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                        ) {
                            StatsCard(
                                label = "Week",
                                value = "${uiState.frequencyStats.thisWeek}",
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                label = "Month",
                                value = "${uiState.frequencyStats.thisMonth}",
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                label = "Year",
                                value = "${uiState.frequencyStats.thisYear}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Column {
                        SectionTitle(title = "Weekly Volume (12 Weeks)")
                        Spacer(modifier = Modifier.height(spacing.small))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.padding(spacing.medium)) {
                                VolumeChart(data = uiState.weeklyVolumeChart)
                            }
                        }
                    }
                }

                item {
                    Column {
                        SectionTitle(title = "Muscle Distribution")
                        Spacer(modifier = Modifier.height(spacing.small))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.padding(spacing.medium)) {
                                DonutChart(data = uiState.muscleDistribution)
                            }
                        }
                    }
                }

                item {
                    ExerciseProgressSection(
                        exercises = uiState.availableExercises,
                        selectedExerciseId = uiState.selectedExerciseId,
                        selectedRange = uiState.selectedRange,
                        progress = uiState.exerciseProgress,
                        onExerciseSelected = { viewModel.onEvent(ProgressUiEvent.OnExerciseSelected(it)) },
                        onRangeSelected = { viewModel.onEvent(ProgressUiEvent.OnTimeRangeSelected(it)) }
                    )
                }

                item {
                    Column {
                        SectionTitle(title = "Consistency Heatmap")
                        Spacer(modifier = Modifier.height(spacing.small))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.padding(spacing.medium)) {
                                ContributionHeatmap(data = uiState.heatmapData)
                            }
                        }
                    }
                }

                if (uiState.insights.isNotEmpty()) {
                    item {
                        Column {
                            SectionTitle(title = "Training Insights")
                            Spacer(modifier = Modifier.height(spacing.small))
                            uiState.insights.forEach { insight ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = spacing.extraSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(spacing.small))
                                    Text(text = insight, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        SectionTitle(title = "Milestones")
                        Spacer(modifier = Modifier.height(spacing.small))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                            contentPadding = PaddingValues(bottom = spacing.medium)
                        ) {
                            items(uiState.milestones) { achievement ->
                                AchievementCard(achievement)
                            }
                        }
                    }
                }

                item {
                    Column {
                        SectionTitle(title = "All Personal Records")
                        Spacer(modifier = Modifier.height(spacing.small))
                        if (uiState.personalRecords.isEmpty()) {
                            Text(text = "No records yet. Keep pushing!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                                uiState.personalRecords.forEach { pr ->
                                    PersonalRecordItem(pr = pr)
                                }
                            }
                        }
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
fun PerformanceScoreCard(score: Int) {
    val spacing = LocalSpacing.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(spacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text(text = "$score", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.width(spacing.large))
            Column {
                Text(text = "Performance Score", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Based on consistency, volume, and progressive overload.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Surface(
        modifier = Modifier.width(160.dp),
        color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = if (achievement.isUnlocked) "🏆" else "🔒", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = achievement.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(text = achievement.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, minLines = 2)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { achievement.progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressSection(
    exercises: List<Exercise>,
    selectedExerciseId: String?,
    selectedRange: TimeRange,
    progress: List<com.example.forgegym.data.models.ExerciseProgress>,
    onExerciseSelected: (String?) -> Unit,
    onRangeSelected: (TimeRange) -> Unit
) {
    val spacing = LocalSpacing.current
    var expanded by remember { mutableStateOf(false) }
    val selectedExercise = exercises.find { it.id == selectedExerciseId }
    val primaryColor = MaterialTheme.colorScheme.primary

    Column {
        SectionTitle(title = "Exercise Progress")
        Spacer(modifier = Modifier.height(spacing.small))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedExercise?.name ?: "Select Exercise",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                exercises.forEach { exercise ->
                    DropdownMenuItem(
                        text = { Text(exercise.name) },
                        onClick = {
                            onExerciseSelected(exercise.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.small))

        ScrollableTabRow(
            selectedTabIndex = selectedRange.ordinal,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {}
        ) {
            TimeRange.entries.forEach { range ->
                val selected = selectedRange == range
                Tab(
                    selected = selected,
                    onClick = { onRangeSelected(range) },
                    text = {
                        Text(
                            text = when(range) {
                                TimeRange.DAYS_7 -> "7D"
                                TimeRange.DAYS_30 -> "30D"
                                TimeRange.DAYS_90 -> "90D"
                                TimeRange.YEAR_1 -> "1Y"
                                TimeRange.ALL -> "ALL"
                            },
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        if (selectedExerciseId == null) {
            EmptyState(message = "Select an exercise to view progression.", icon = Icons.Default.Info)
        } else if (progress.isEmpty()) {
            EmptyState(message = "No data available for this period.", icon = Icons.Default.Info)
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.padding(spacing.medium)) {
                    val maxVal = (progress.maxOf { it.maxWeight }).coerceAtLeast(1.0)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val stepX = canvasWidth / (progress.size.coerceAtLeast(2) - 1)
                        progress.forEachIndexed { index, p ->
                            val x = index * stepX
                            val y = (1 - (p.maxWeight / maxVal)) * canvasHeight
                            if (index > 0) {
                                val prevY = (1 - (progress[index - 1].maxWeight / maxVal)) * canvasHeight
                                drawLine(
                                    color = primaryColor,
                                    start = Offset((index - 1) * stepX, prevY.toFloat()),
                                    end = Offset(x, y.toFloat()),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(x, y.toFloat()))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(spacing.small))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val start = progress.first().maxWeight
                val end = progress.last().maxWeight
                val diff = end - start
                val percent = if (start > 0) (diff / start * 100).toInt() else 0
                Text(text = "Improvement: ${if (diff >= 0) "+" else ""}$percent%", style = MaterialTheme.typography.labelMedium, color = if (diff >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                Text(text = "Best: ${progress.maxOf { it.maxWeight }} kg", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
