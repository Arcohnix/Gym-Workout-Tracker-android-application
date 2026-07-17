package com.example.forgegym.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.viewmodel.OnboardingUiEvent
import com.example.forgegym.viewmodel.OnboardingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onNextClick: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (uiState.dateOfBirth > 0) uiState.dateOfBirth else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(OnboardingUiEvent.OnDobChanged(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.medium)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = uiState.name.isNotBlank() && uiState.height.isNotBlank() && uiState.weight.isNotBlank()
            ) {
                Text("NEXT", fontWeight = FontWeight.ExtraBold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(spacing.medium)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(spacing.large))
            Text(
                text = "Welcome to ForgeGym",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Let's set up your combat profile",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(spacing.extraLarge))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onEvent(OnboardingUiEvent.OnNameChanged(it)) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(spacing.medium))

            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Date of Birth", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = if (uiState.dateOfBirth > 0) {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(uiState.dateOfBirth))
                            } else "Select your birthday",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.medium))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
                OutlinedTextField(
                    value = uiState.height,
                    onValueChange = { viewModel.onEvent(OnboardingUiEvent.OnHeightChanged(it)) },
                    label = { Text("Height (cm)") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.weight,
                    onValueChange = { viewModel.onEvent(OnboardingUiEvent.OnWeightChanged(it)) },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(spacing.medium))
            
            OutlinedTextField(
                value = uiState.goalWeight,
                onValueChange = { viewModel.onEvent(OnboardingUiEvent.OnGoalWeightChanged(it)) },
                label = { Text("Goal Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )
        }
    }
}
