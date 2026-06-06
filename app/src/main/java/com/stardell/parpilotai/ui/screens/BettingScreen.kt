package com.stardell.parpilotai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
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
import com.stardell.parpilotai.models.*
import com.stardell.parpilotai.ui.theme.LocalParPilotColors
import com.stardell.parpilotai.viewmodel.GolferViewModel
import java.util.UUID
import kotlin.math.abs

@Composable
fun BettingScreen(viewModel: GolferViewModel, onDismiss: () -> Unit) {
    val activeScorecard by viewModel.activeScorecard.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val golfers by viewModel.golfers.collectAsState()

    val colors = LocalParPilotColors.current
    var selectedSidebarTab by remember { mutableStateOf("overview") }

    val course = courses.find { it.id == activeScorecard?.courseId }
    val activeGolfers = golfers.filter { activeScorecard?.golferIds?.contains(it.id) == true }

    if (activeScorecard == null || course == null) {
        Box(modifier = Modifier.fillMaxSize().background(colors.backgroundStart), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No active scorecard found.", color = Color.White, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) { Text("Dismiss") }
            }
        }
        return
    }

    Row(modifier = Modifier.fillMaxSize().background(colors.backgroundStart)) {
        // Sidebar View
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(Brush.linearGradient(listOf(Color(0xFF051D21), Color(0xFF0B0C0E))))
        ) {
            // Title
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
                Text("ROUND WAGERS", color = Color(0xFFC5A059), fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                Text(course.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1)
            }
            Divider(color = Color.White.copy(alpha = 0.1f))

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 8.dp)) {
                item {
                    SidebarButton(
                        title = "Handshake & Settle",
                        isSelected = selectedSidebarTab == "overview",
                        onClick = { selectedSidebarTab = "overview" }
                    )
                }
                val games = activeScorecard!!.bettingGames ?: emptyList()
                items(games) { game ->
                    SidebarButton(
                        title = game.name,
                        isSelected = selectedSidebarTab == game.id.toString(),
                        onClick = { selectedSidebarTab = game.id.toString() }
                    )
                }
            }

            // Add Game Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { /* TODO: Show Add Game Modal */ }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Bet", color = Color.White, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Divider(modifier = Modifier.width(1.dp).fillMaxHeight(), color = Color.White.copy(alpha = 0.1f))

        // Detail View
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (selectedSidebarTab == "overview") {
                HandshakeOverview(
                    scorecard = activeScorecard!!,
                    course = course,
                    golfers = activeGolfers,
                    onDismiss = onDismiss
                )
            } else {
                val game = activeScorecard!!.bettingGames?.find { it.id.toString() == selectedSidebarTab }
                if (game != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Game Details for ${game.name}", color = Color.White, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Handshake, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun HandshakeOverview(
    scorecard: Scorecard,
    course: Course,
    golfers: List<Golfer>,
    onDismiss: () -> Unit
) {
    val settlement = BettingCalculator.calculateSettlement(scorecard, course, golfers)
    val games = scorecard.bettingGames ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Handshake & Settle", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))) {
                Text("Close", color = Color.White, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (games.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No wagers recorded for this round. Add a game to get started!", color = Color.Gray, fontFamily = FontFamily.Monospace)
            }
            return
        }

        // Net Standings
        Text("Net Standings", color = Color(0xFFC5A059), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            settlement.netPayouts.forEach { (id, amt) ->
                val golfer = golfers.find { it.id == id } ?: return@forEach
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(golfer.name, color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(8.dp))
                        val formattedAmt = String.format("$%.2f", abs(amt))
                        Text(
                            text = if (amt > 0) "+$formattedAmt" else if (amt < 0) "-$formattedAmt" else "Even",
                            color = if (amt > 0) Color.Green else if (amt < 0) Color.Red else Color.Gray,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Venmo Transfers
        Text("Who Pays Who", color = Color(0xFFC5A059), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))

        if (settlement.transfers.isEmpty()) {
            Text("All square! No money changing hands.", color = Color.Gray, fontFamily = FontFamily.Monospace)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(settlement.transfers) { transfer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${transfer.fromName} owes ${transfer.toName}", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                        Text(String.format("$%.2f", transfer.amount), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
