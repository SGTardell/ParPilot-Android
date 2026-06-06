package com.stardell.parpilotai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stardell.parpilotai.R
import com.stardell.parpilotai.models.Golfer
import com.stardell.parpilotai.ui.theme.LocalParPilotColors
import com.stardell.parpilotai.viewmodel.GolferViewModel
import java.util.UUID

@Composable
fun GolfersScreen(viewModel: GolferViewModel) {
    val golfers by viewModel.golfers.collectAsState()
    val activeScorecard by viewModel.activeScorecard.collectAsState()
    
    val sortedGolfers = golfers.sortedBy { it.name.lowercase() }
    var selectedGolfers by remember { mutableStateOf(setOf<UUID>()) }

    val colors = LocalParPilotColors.current
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(colors.backgroundStart, colors.backgroundEnd)
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
                        text = "GOLFERS",
                        color = colors.headerText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Manage players and handicaps",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { /* TODO: Show Settings */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit, // TODO: Use figure.golf equivalent
                        contentDescription = "My Clubs",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "My Clubs",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Table Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.width(45.dp)) // Offset for checkbox
                Text(
                    text = "NAME",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "HCP",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(30.dp)) // Offset for edit pencil
            }

            // List of Golfers
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedGolfers) { golfer ->
                    val isSelected = selectedGolfers.contains(golfer.id)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox
                        Box(
                            modifier = Modifier
                                .size(25.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedGolfers = if (isSelected) {
                                        selectedGolfers - golfer.id
                                    } else {
                                        selectedGolfers + golfer.id
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))

                        // Name
                        Text(
                            text = golfer.name.ifEmpty { "Unnamed Golfer" },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )

                        // Handicap Box
                        Box(
                            modifier = Modifier
                                .width(45.dp)
                                .height(25.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = golfer.handicap?.toString() ?: "-",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Edit Pencil
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { /* TODO: Open Golfer Profile View */ }
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(top = 10.dp))
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add New Golfer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { /* TODO: Add New Golfer */ }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add New Golfer", color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }

                // Add to Scorecard
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable {
                            if (activeScorecard != null) {
                                // TODO: Add logic to viewmodel
                                // val selected = golfers.filter { selectedGolfers.contains(it.id) }
                                // viewModel.addGolfersToActiveScorecard(selected)
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add to Scorecard", color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
