package com.stardell.parpilotai.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stardell.parpilotai.models.Course
import com.stardell.parpilotai.models.Golfer
import com.stardell.parpilotai.models.Hole
import com.stardell.parpilotai.models.Scorecard
import com.stardell.parpilotai.ui.theme.LocalParPilotColors
import com.stardell.parpilotai.viewmodel.GolferViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardScreen(viewModel: GolferViewModel) {
    val activeScorecard by viewModel.activeScorecard.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val golfers by viewModel.golfers.collectAsState()
    
    val colors = LocalParPilotColors.current
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(colors.backgroundStart, colors.backgroundEnd)
    )

    var showFinishAlert by remember { mutableStateOf(false) }
    var showBettingScreen by remember { mutableStateOf(false) }

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
            val course = courses.find { it.id == activeScorecard?.courseId }

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
                        text = "SCORECARD",
                        color = colors.headerText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (course != null) {
                        Text(
                            text = course.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (activeScorecard != null) {
                        // Betting Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(0.5.dp, Color(0xFFC5A059), RoundedCornerShape(8.dp))
                                .clickable { showBettingScreen = true }
                                .padding(10.dp)
                        ) {
                            Text("$", color = Color(0xFFC5A059), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        // Map
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { /* Show Map */ }
                                .padding(10.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Map", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        
                        // Finish Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { showFinishAlert = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Finish", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(60.dp))
                    }
                }
            }

            if (activeScorecard != null && course != null) {
                val activeGolfers = golfers.filter { activeScorecard!!.golferIds.contains(it.id) }
                if (activeGolfers.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Add golfers from the Golfers tab", color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                    }
                } else {
                    ScorecardGrid(
                        course = course,
                        scorecard = activeScorecard!!,
                        golfers = activeGolfers,
                        viewModel = viewModel
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Select a course from the Courses tab to start a scorecard.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    if (showFinishAlert) {
        AlertDialog(
            onDismissRequest = { showFinishAlert = false },
            title = { Text("Finish Round?") },
            text = { Text("Are you sure you want to finish this round? You can save it to history, or discard it entirely.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.completeActiveRound()
                    showFinishAlert = false
                }) { Text("Save to History") }
            },
            dismissButton = {
                TextButton(onClick = {
                    // To discard entirely, we can just start a new state or add discard method
                    // For now, let's just clear active scorecard via a viewModel method
                    // viewModel.discardActiveRound() (Needs implementing)
                    showFinishAlert = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showBettingScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            BettingScreen(viewModel = viewModel, onDismiss = { showBettingScreen = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardGrid(
    course: Course,
    scorecard: Scorecard,
    golfers: List<Golfer>,
    viewModel: GolferViewModel
) {
    val scrollState = rememberScrollState()
    var selectedHole by remember { mutableStateOf<Hole?>(null) }
    var selectedGolfer by remember { mutableStateOf<Golfer?>(null) }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Fixed Labels Column
        Column(
            modifier = Modifier
                .width(130.dp)
                .background(LocalParPilotColors.current.backgroundStart)
                .shadow(3.dp)
        ) {
            DataCell(text = "Hole", isBold = true)
            DataCell(text = "Yds")
            DataCell(text = "Par")
            golfers.forEach { golfer ->
                val hcpText = if (golfer.handicap == null || golfer.handicap == 0) "NH" else golfer.handicap.toString()
                DataCell(text = "${golfer.name} ($hcpText)")
            }
            DataCell(text = "Hcp")
        }

        // Horizontally Scrollable Data Columns
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
        ) {
            // Front 9
            for (i in 1..9) {
                course.holes.find { it.number == i }?.let { hole ->
                    HoleColumn(
                        hole = hole,
                        course = course,
                        scorecard = scorecard,
                        golfers = golfers,
                        onScoreClick = { g ->
                            selectedGolfer = g
                            selectedHole = hole
                        }
                    )
                }
            }
            // OUT Total
            TotalColumn(title = "OUT", holes = course.holes.filter { it.number <= 9 }, scorecard = scorecard, golfers = golfers)

            // Back 9
            for (i in 10..18) {
                course.holes.find { it.number == i }?.let { hole ->
                    HoleColumn(
                        hole = hole,
                        course = course,
                        scorecard = scorecard,
                        golfers = golfers,
                        onScoreClick = { g ->
                            selectedGolfer = g
                            selectedHole = hole
                        }
                    )
                }
            }
            // IN Total
            TotalColumn(title = "IN", holes = course.holes.filter { it.number > 9 }, scorecard = scorecard, golfers = golfers)
            
            // TOT Total
            TotalColumn(title = "TOT", holes = course.holes, scorecard = scorecard, golfers = golfers)
        }
    }

    if (selectedGolfer != null && selectedHole != null) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedGolfer = null
                selectedHole = null
            }
        ) {
            ScoreInputSheetContent(
                golfer = selectedGolfer!!,
                hole = selectedHole!!,
                viewModel = viewModel,
                onDismiss = {
                    selectedGolfer = null
                    selectedHole = null
                }
            )
        }
    }
}

@Composable
fun ScoreInputSheetContent(
    golfer: Golfer,
    hole: Hole,
    viewModel: GolferViewModel,
    onDismiss: () -> Unit
) {
    val chips by viewModel.scoreChips.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${golfer.name} - Hole ${hole.number} (Par ${hole.par})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Simple Grid for Chips
        val chunkedChips = chips.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            chunkedChips.forEach { rowChips ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowChips.forEach { chip ->
                        Button(
                            onClick = {
                                val relativeScore = if (chip.title == "ACE") -(hole.par - 1) else chip.relativeScore
                                val finalScore = hole.par + relativeScore
                                val safeScore = maxOf(1, finalScore)
                                viewModel.updateScore(golfer.id, hole.number, safeScore)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text(text = chip.title, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Delete Button
        Box(
            modifier = Modifier
                .size(80.dp, 60.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .clickable {
                    viewModel.updateScore(golfer.id, hole.number, null)
                    onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Black)
        }
        
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun HoleColumn(
    hole: Hole,
    course: Course,
    scorecard: Scorecard,
    golfers: List<Golfer>,
    onScoreClick: (Golfer) -> Unit
) {
    Column(modifier = Modifier.width(44.dp)) {
        DataCell(text = hole.number.toString(), isBold = true)
        DataCell(text = hole.yardage.toString())
        DataCell(text = hole.par.toString())
        
        golfers.forEach { golfer ->
            val score = scorecard.scores["${golfer.id}_${hole.number}"] ?: 0
            Box(
                modifier = Modifier
                    .size(44.dp, 32.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f))
                    .clickable { onScoreClick(golfer) },
                contentAlignment = Alignment.Center
            ) {
                if (score != 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = score.toString(),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                        // TODO: Implement Birdie/Bogey Shape Overlay
                    }
                }
            }
        }
        
        DataCell(text = hole.handicap.toString())
    }
}

@Composable
fun TotalColumn(
    title: String,
    holes: List<Hole>,
    scorecard: Scorecard,
    golfers: List<Golfer>
) {
    Column(modifier = Modifier.width(44.dp)) {
        DataCell(text = title, isBold = true)
        DataCell(text = holes.sumOf { it.yardage }.toString())
        DataCell(text = holes.sumOf { it.par }.toString())
        
        golfers.forEach { golfer ->
            val total = holes.sumOf { hole -> scorecard.scores["${golfer.id}_${hole.number}"] ?: 0 }
            if (total == 0) {
                DataCell(text = "")
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp, 32.dp)
                        .background(Color.White)
                        .border(0.5.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = total.toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        DataCell(text = "")
    }
}

@Composable
fun DataCell(text: String, isBold: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .border(0.5.dp, Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}
