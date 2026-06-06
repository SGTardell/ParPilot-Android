package com.stardell.parpilotai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stardell.parpilotai.models.Course
import com.stardell.parpilotai.ui.theme.LocalParPilotColors
import com.stardell.parpilotai.viewmodel.GolferViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(viewModel: GolferViewModel) {
    val courses by viewModel.courses.collectAsState()
    val activeScorecard by viewModel.activeScorecard.collectAsState()
    var searchText by remember { mutableStateOf("") }
    
    val filteredCourses = if (searchText.isEmpty()) {
        courses
    } else {
        courses.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    val colors = LocalParPilotColors.current
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(colors.backgroundStart, colors.backgroundEnd)
    )
    val dashGradient = Brush.linearGradient(
        colors = listOf(colors.actionTeal, colors.actionTeal.copy(alpha = 0.8f))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 85.dp) // Bottom padding for Nav Bar
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                // TODO: Add Image Par Pilot Wings here
                Spacer(modifier = Modifier.width(60.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "COURSES",
                        color = colors.headerText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (activeScorecard == null) "Select a course to play" else "Active scorecard in progress",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Spacer(modifier = Modifier.width(60.dp))
            }

            // Search Bar
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(dashGradient)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                placeholder = { Text("Search courses...", color = Color.White.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Search Online Button
            Button(
                onClick = { /* TODO: Show Online Search Sheet */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 10.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues() // Remove default padding for gradient
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(dashGradient)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search Online Courses",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // List of Courses
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCourses) { course ->
                    CourseListItem(
                        course = course,
                        isActive = activeScorecard?.courseId == course.id,
                        colors = colors,
                        onPlayClick = { /* TODO: Quick Start Sheet */ },
                        onEditClick = { /* TODO: Edit Sheet */ },
                        onMapClick = { /* TODO: Map Sheet */ }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
fun CourseListItem(
    course: Course,
    isActive: Boolean,
    colors: com.stardell.parpilotai.ui.theme.ParPilotColors,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    onMapClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name,
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${course.holes.size} Holes",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.actionTeal)
                    .clickable(onClick = onPlayClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Play",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Edit Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onEditClick)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            // Map Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onMapClick)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Map", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            if (isActive) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = colors.actionTeal)
            }
        }
    }
}
