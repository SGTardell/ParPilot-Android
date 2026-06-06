package com.stardell.parpilotai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.stardell.parpilotai.ui.screens.MainScreen
import com.stardell.parpilotai.ui.theme.ParPilotAITheme
import com.stardell.parpilotai.viewmodel.GolferViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GolferViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by viewModel.themePreference.collectAsState()
            val appStyleTheme by viewModel.appStyleTheme.collectAsState()
            
            ParPilotAITheme(
                appTheme = appTheme,
                appStyleTheme = appStyleTheme
            ) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}