package com.example.forgegym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.forgegym.data.local.preferences.PreferencesManager
import com.example.forgegym.data.models.ThemeMode
import com.example.forgegym.navigation.AppNavigation
import com.example.forgegym.ui.theme.ForgeGymTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by preferencesManager.userPreferencesFlow.collectAsState(initial = null)
            val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsState(initial = null)
            val themeMode = preferences?.themeMode ?: ThemeMode.DARK

            ForgeGymTheme(themeMode = themeMode) {
                if (isOnboardingCompleted != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        val navController = rememberNavController()
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            AppNavigation(
                                navController = navController,
                                isOnboardingCompleted = isOnboardingCompleted!!
                            )
                        }
                    }
                }
            }
        }
    }
}
