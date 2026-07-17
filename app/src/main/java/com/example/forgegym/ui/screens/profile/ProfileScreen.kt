package com.example.forgegym.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.ui.components.SectionTitle
import com.example.forgegym.ui.components.StatsCard
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.ProfileViewModel
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

                ProfileHeader(
                    name = uiState.userProfile.name,
                    onEditClick = { /* Show Edit Profile Dialog */ }
                )

                Spacer(modifier = Modifier.height(spacing.extraLarge))

                SectionTitle(title = "Physical Metrics")
                Spacer(modifier = Modifier.height(spacing.small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    StatsCard(
                        label = "Weight",
                        value = "${uiState.userProfile.currentWeight} kg",
                        subLabel = "Goal: ${uiState.userProfile.goalWeight} kg",
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        label = "BMI",
                        value = String.format(Locale.getDefault(), "%.1f", uiState.userProfile.bmi),
                        subLabel = getBmiCategory(uiState.userProfile.bmi),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                StatsCard(
                    label = "Height",
                    value = "${uiState.userProfile.height} cm",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.large))

                SectionTitle(title = "Workout Statistics")
                Spacer(modifier = Modifier.height(spacing.small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    StatsCard(
                        label = "Workouts",
                        value = "${uiState.totalWorkouts}",
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        label = "PRs",
                        value = "${uiState.prCount}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    StatsCard(
                        label = "Total Time",
                        value = "${uiState.totalTimeMinutes}m",
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        label = "Total Volume",
                        value = "${(uiState.totalVolume / 1000).toInt()}k",
                        subLabel = "kg lifted",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                SectionTitle(title = "Body Measurements")
                Spacer(modifier = Modifier.height(spacing.small))

                if (uiState.latestMeasurement == null) {
                    Text(
                        text = "No measurements recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    MeasurementSummary(measurement = uiState.latestMeasurement!!)
                }

                Spacer(modifier = Modifier.height(spacing.huge))
            }
        }
    }
}

@Composable
fun ProfileHeader(
    name: String,
    onEditClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Premium Member",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
        }
    }
}

@Composable
fun MeasurementSummary(measurement: com.example.forgegym.data.models.BodyMeasurement) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MeasurementItem(label = "Chest", value = measurement.chest)
                MeasurementItem(label = "Waist", value = measurement.waist)
                MeasurementItem(label = "Hips", value = measurement.hips)
            }
        }
    }
}

@Composable
fun MeasurementItem(label: String, value: Double?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = if (value != null) "${value}cm" else "-",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getBmiCategory(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Healthy"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }
}
