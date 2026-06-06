package com.stardell.parpilotai.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stardell.parpilotai.viewmodel.GolferViewModel

enum class NavigationItem(val title: String, val icon: ImageVector, val index: Int) {
    HOME("Home", Icons.Default.Home, 0),
    COURSES("Courses", Icons.Default.Place, 1),
    GOLFERS("Golfers", Icons.Default.Person, 2),
    SCORECARD("Scorecard", Icons.Default.List, 3),
    HISTORY("History", Icons.Default.DateRange, 4)
}

@Composable
fun MainScreen(viewModel: GolferViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        bottomBar = {
            if (selectedTab != 0) { // Same logic as iOS: Home hides the background of the bottom bar or Home has its own overlay
                NavigationBar(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    NavigationItem.values().forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (selectedTab == item.index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            },
                            selected = selectedTab == item.index,
                            onClick = {
                                viewModel.updateSelectedTab(item.index)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                selectedTextColor = Color.White,
                                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Main View Switcher
        Crossfade(targetState = selectedTab, modifier = Modifier.padding(innerPadding)) { tabIndex ->
            when (tabIndex) {
                0 -> HomeScreen(viewModel = viewModel, onSettingsClick = { /* Open Settings */ })
                1 -> Box(modifier = Modifier.fillMaxSize().background(Color.Red)) // TODO: CourseScreen
                2 -> Box(modifier = Modifier.fillMaxSize().background(Color.Blue)) // TODO: GolfersScreen
                3 -> Box(modifier = Modifier.fillMaxSize().background(Color.Green)) // TODO: ScorecardScreen
                4 -> Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) // TODO: HistoryScreen
                else -> HomeScreen(viewModel = viewModel, onSettingsClick = {})
            }
        }
    }
}
