package com.example.forgegym.ui.screens.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.data.models.Exercise
import com.example.forgegym.ui.components.SectionTitle
import com.example.forgegym.ui.components.StatsCard
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.ExerciseDetailUiEvent
import com.example.forgegym.viewmodel.ExerciseDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.exercise?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.exercise?.let { exercise ->
                        IconButton(onClick = { viewModel.onEvent(ExerciseDetailUiEvent.OnToggleFavorite(!exercise.isFavorite)) }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = if (exercise.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            )
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
            val exercise = uiState.exercise!!
            val stats = uiState.stats
            val primaryColor = MaterialTheme.colorScheme.primary

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header / Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exercise.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }

                Column(modifier = Modifier.padding(horizontal = spacing.medium)) {
                    // Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        MetadataChip(label = exercise.primaryMuscleGroup.name)
                        MetadataChip(label = exercise.equipment.name)
                        MetadataChip(label = exercise.difficulty.name)
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Training History Stats
                    SectionTitle(title = "Your History")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        StatsCard(
                            label = "Best Volume",
                            value = "${stats.bestVolume.toInt()}kg",
                            modifier = Modifier.weight(1f)
                        )
                        StatsCard(
                            label = "Est. 1RM",
                            value = "${stats.estimated1RM.toInt()}kg",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        StatsCard(
                            label = "Avg Weight",
                            value = String.format(Locale.getDefault(), "%.1fkg", stats.averageWeight),
                            modifier = Modifier.weight(1f)
                        )
                        StatsCard(
                            label = "Total Sessions",
                            value = "${stats.totalSessions}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Progression Timeline
                    if (stats.progression.isNotEmpty()) {
                        SectionTitle(title = "Progression Timeline")
                        Spacer(modifier = Modifier.height(spacing.small))
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.padding(spacing.medium)) {
                                val maxVal = (stats.progression.maxOf { it.maxWeight }).coerceAtLeast(1.0)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    val stepX = canvasWidth / (stats.progression.size.coerceAtLeast(2) - 1)
                                    stats.progression.forEachIndexed { index, p ->
                                        val x = index * stepX
                                        val y = (1 - (p.maxWeight / maxVal)) * canvasHeight
                                        if (index > 0) {
                                            val prevY = (1 - (stats.progression[index - 1].maxWeight / maxVal)) * canvasHeight
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
                        Spacer(modifier = Modifier.height(spacing.large))
                    }

                    // How to Perform
                    if (exercise.instructions.isNotEmpty()) {
                        SectionTitle(title = "How To Perform")
                        exercise.instructions.forEachIndexed { index, step ->
                            Row(modifier = Modifier.padding(vertical = spacing.extraSmall)) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(spacing.small))
                                Text(text = step, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    // Tips & Mistakes
                    if (exercise.tips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(spacing.large))
                        SectionTitle(title = "Pro Tips")
                        exercise.tips.forEach { tip ->
                            Text(text = "• $tip", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }

                    if (exercise.commonMistakes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(spacing.large))
                        SectionTitle(title = "Common Mistakes")
                        exercise.commonMistakes.forEach { mistake ->
                            Text(
                                text = "• $mistake", 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Recommendations
                    SectionTitle(title = "Recommendations")
                    RecommendationItem(label = "Rep Range", value = "${exercise.recommendedRepRange.first} - ${exercise.recommendedRepRange.last}")
                    RecommendationItem(label = "Set Range", value = "${exercise.recommendedSetRange.first} - ${exercise.recommendedSetRange.last}")
                    RecommendationItem(label = "Rest Time", value = "${exercise.restRecommendationSeconds}s")
                    
                    Spacer(modifier = Modifier.height(spacing.huge))
                }
            }
        }
    }
}

@Composable
fun MetadataChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RecommendationItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
