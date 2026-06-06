package com.stardell.parpilotai.models

import java.util.UUID

enum class GameType(val displayName: String) {
    SKINS("Skins"),
    NASSAU("Nassau"),
    MATCH_PLAY("Match Play"),
    STABLEFORD("Stableford"),
    CLOSEST_TO_PIN("Closest to Pin"),
    WOLF("Wolf"),
    BIRDIES_AND_EAGLES("Birdies & Eagles")
}

enum class StablefordType(val displayName: String) {
    STANDARD("Standard"),
    MODIFIED("Modified")
}

enum class MatchFormat(val displayName: String) {
    INDIVIDUAL("Individual"),
    TEAM("Team (Best Ball)")
}

data class WolfHoleConfig(
    var wolfPlayerId: UUID,
    var partnerPlayerId: UUID? = null // null for Lone Wolf
)

data class BettingGame(
    val id: UUID = UUID.randomUUID(),
    var name: String,
    var gameType: GameType,
    var wagerAmount: Double,
    var useNetScore: Boolean,
    
    // Skins specific
    var skinsCarryOver: Boolean = true,
    
    // Nassau & Match Play specific
    var player1Id: UUID? = null,
    var player2Id: UUID? = null,
    var matchFormat: MatchFormat? = MatchFormat.INDIVIDUAL,
    var team1PlayerIds: List<UUID>? = emptyList(),
    var team2PlayerIds: List<UUID>? = emptyList(),
    
    // Stableford specific
    var stablefordType: StablefordType = StablefordType.STANDARD,
    
    // Closest to Pin specific
    // Map of hole number (Int) to winning golfer (UUID)
    var closestToPinWinners: Map<Int, UUID> = emptyMap(),
    
    // Wolf specific
    // Map of hole number (Int) to WolfHoleConfig
    var wolfHoleConfigs: Map<Int, WolfHoleConfig> = emptyMap(),
    
    // Birdies & Eagles specific
    var birdieWager: Double = 1.0,
    var eagleWager: Double = 2.0
)
