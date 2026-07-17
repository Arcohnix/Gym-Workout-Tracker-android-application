package com.example.forgegym.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forgegym.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBackClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(spacing.medium)
                .verticalScroll(rememberScrollState())
        ) {
            PrivacyItem(
                title = "100% Offline",
                description = "Every piece of data you enter into ForgeGym is stored locally on your device. We do not use any cloud servers to store your workout history.",
                icon = Icons.Default.Storage
            )
            Spacer(modifier = Modifier.height(spacing.large))
            PrivacyItem(
                title = "No Tracking",
                description = "We do not use any analytics or tracking tools. Your training habits are yours and yours alone.",
                icon = Icons.Default.Security
            )
            Spacer(modifier = Modifier.height(spacing.large))
            PrivacyItem(
                title = "No Accounts",
                description = "ForgeGym does not require you to create an account. There are no passwords to remember and no personal data collected during sign-up.",
                icon = Icons.Default.Lock
            )
            Spacer(modifier = Modifier.height(spacing.large))
            Text(
                text = "Your Data, Your Ownership",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Since everything is offline, you are responsible for your data. We recommend using the Backup feature regularly to avoid data loss if you change or lose your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PrivacyItem(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
