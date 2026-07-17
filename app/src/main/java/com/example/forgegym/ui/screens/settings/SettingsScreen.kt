package com.example.forgegym.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.forgegym.data.models.*
import com.example.forgegym.ui.theme.LocalSpacing
import com.example.forgegym.util.FileUtil
import com.example.forgegym.viewmodel.SettingsUiEvent
import com.example.forgegym.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            uiState.backupJson?.let { json ->
                if (FileUtil.writeFileToUri(context, it, json)) {
                    viewModel.onEvent(SettingsUiEvent.ClearBackupState)
                }
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            uiState.historyCsv?.let { csv ->
                if (FileUtil.writeFileToUri(context, it, csv)) {
                    viewModel.onEvent(SettingsUiEvent.ClearBackupState)
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val json = FileUtil.readFileFromUri(context, it)
            json?.let { content ->
                viewModel.onEvent(SettingsUiEvent.OnRestoreData(content))
            }
        }
    }

    LaunchedEffect(uiState.backupJson) {
        if (uiState.backupJson != null) {
            exportBackupLauncher.launch("ForgeGym_Backup_${System.currentTimeMillis()}.json")
        }
    }

    LaunchedEffect(uiState.historyCsv) {
        if (uiState.historyCsv != null) {
            exportCsvLauncher.launch("ForgeGym_History_${System.currentTimeMillis()}.csv")
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete All Data?") },
            text = { Text("This will permanently remove all your workouts, history, and records. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.onEvent(SettingsUiEvent.OnDeleteAllData)
                    showDeleteConfirm = false
                }) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("CANCEL") }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset App?") },
            text = { Text("This will delete all data and reset all your preferences to default. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.onEvent(SettingsUiEvent.OnResetApp)
                    showResetConfirm = false
                }) {
                    Text("RESET", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("CANCEL") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    .padding(innerPadding)
            ) {
                item {
                    SettingsHeader("Appearance")
                    SettingsThemeModeSelector(
                        selected = uiState.preferences.themeMode,
                        onModeSelected = { viewModel.onEvent(SettingsUiEvent.OnThemeModeChanged(it)) }
                    )
                    SettingsToggleItem(
                        label = "AMOLED Mode",
                        icon = Icons.Default.Brightness2,
                        checked = uiState.preferences.isAmoledMode,
                        onToggle = { viewModel.onEvent(SettingsUiEvent.OnAmoledToggle(it)) }
                    )
                    SettingsToggleItem(
                        label = "Dynamic Color",
                        icon = Icons.Default.Palette,
                        checked = uiState.preferences.isDynamicColorEnabled,
                        onToggle = { viewModel.onEvent(SettingsUiEvent.OnDynamicColorToggle(it)) }
                    )
                }

                item {
                    SettingsHeader("Units")
                    SettingsUnitSelector(
                        label = "Weight Unit",
                        icon = Icons.Default.MonitorWeight,
                        value = uiState.preferences.weightUnit.name,
                        onClick = {
                            val next = if (uiState.preferences.weightUnit == WeightUnit.KG) WeightUnit.LBS else WeightUnit.KG
                            viewModel.onEvent(SettingsUiEvent.OnWeightUnitChanged(next))
                        }
                    )
                    SettingsUnitSelector(
                        label = "Height Unit",
                        icon = Icons.Default.Height,
                        value = uiState.preferences.heightUnit.name,
                        onClick = {
                            val next = if (uiState.preferences.heightUnit == HeightUnit.CM) HeightUnit.FT_IN else HeightUnit.CM
                            viewModel.onEvent(SettingsUiEvent.OnHeightUnitChanged(next))
                        }
                    )
                }

                item {
                    SettingsHeader("Workout")
                    SettingsClickItem(
                        label = "Default Rest Timer",
                        icon = Icons.Default.Timer,
                        subtitle = "${uiState.preferences.defaultRestTimerSeconds}s",
                        onClick = { /* Timer picker */ }
                    )
                    SettingsToggleItem(
                        label = "Auto Start Rest",
                        icon = Icons.Default.PlayArrow,
                        checked = uiState.preferences.isAutoStartRestTimer,
                        onToggle = { viewModel.onEvent(SettingsUiEvent.OnAutoStartRestToggle(it)) }
                    )
                    SettingsToggleItem(
                        label = "Auto Resume Workout",
                        icon = Icons.Default.Refresh,
                        checked = uiState.preferences.isAutoResumeWorkout,
                        onToggle = { viewModel.onEvent(SettingsUiEvent.OnAutoResumeToggle(it)) }
                    )
                    SettingsToggleItem(
                        label = "Confirm Before Ending",
                        icon = Icons.Default.CheckCircleOutline,
                        checked = uiState.preferences.isConfirmEndWorkout,
                        onToggle = { viewModel.onEvent(SettingsUiEvent.OnConfirmEndToggle(it)) }
                    )
                }

                item {
                    SettingsHeader("Data Management")
                    SettingsClickItem(
                        label = "Export Backup",
                        icon = Icons.Default.Upload,
                        subtitle = "Create a versioned JSON backup",
                        onClick = { viewModel.onEvent(SettingsUiEvent.OnBackupClick) }
                    )
                    SettingsClickItem(
                        label = "Import Backup",
                        icon = Icons.Default.Download,
                        subtitle = "Restore data from a JSON file",
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    )
                    SettingsClickItem(
                        label = "Export CSV",
                        icon = Icons.Default.TableChart,
                        subtitle = "Compatible with Excel / Sheets",
                        onClick = { viewModel.onEvent(SettingsUiEvent.OnExportCsvClick) }
                    )
                    SettingsClickItem(
                        label = "Delete All Data",
                        icon = Icons.Default.DeleteForever,
                        onClick = { showDeleteConfirm = true }
                    )
                    SettingsClickItem(
                        label = "Reset App",
                        icon = Icons.Default.Refresh,
                        onClick = { showResetConfirm = true }
                    )
                }

                item {
                    SettingsHeader("Legal & Info")
                    SettingsClickItem(label = "Privacy Policy", icon = Icons.Default.Lock, onClick = onPrivacyClick)
                    SettingsClickItem(label = "About ForgeGym", icon = Icons.Default.Info, onClick = onAboutClick)
                }

                item { Spacer(modifier = Modifier.height(spacing.huge)) }
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    val spacing = LocalSpacing.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.small)
    )
}

@Composable
fun SettingsThemeModeSelector(
    selected: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.medium, vertical = spacing.small),
        horizontalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        ThemeMode.entries.forEach { mode ->
            FilterChip(
                selected = selected == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SettingsUnitSelector(label: String, icon: ImageVector, value: String, onClick: () -> Unit) {
    SettingsClickItem(label = label, icon = icon, subtitle = value, onClick = onClick)
}

@Composable
fun SettingsClickItem(label: String, icon: ImageVector, subtitle: String? = null, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Surface(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(label: String, icon: ImageVector, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val spacing = LocalSpacing.current
    Surface {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(spacing.medium))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
