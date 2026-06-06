package com.stardell.parpilotai.models

import java.util.Date
import java.util.UUID
import kotlin.math.roundToInt

data class Scorecard(
    val id: UUID = UUID.randomUUID(),
    var date: Date = Date(),
    var courseId: UUID,
    var courseNameSnapshot: String? = null,
    var golferIds: List<UUID>,
    var notes: String = "",
    var bettingGames: List<BettingGame>? = null,
    var isUpcoming: Boolean = false,
    
    // Map of key "${golferId}_${holeNumber}" to score
    var scores: MutableMap<String, Int> = mutableMapOf(),
    
    // Map of unique transfer key "${fromId}_${toId}" to settlement method ("Cash" or "Venmo")
    var settledTransfers: MutableMap<String, String>? = mutableMapOf()
) {
    fun getScore(golferId: UUID, hole: Int): Int? {
        return scores["${golferId}_${hole}"]
    }
    
    fun setScore(golferId: UUID, hole: Int, score: Int?) {
        val key = "${golferId}_${hole}"
        if (score != null) {
            scores[key] = score
        } else {
            scores.remove(key)
        }
    }
    
    // MARK: - Calculations
    
    fun courseHandicap(golfer: Golfer, course: Course, useSlope: Boolean = true): Int {
        val index = golfer.handicap ?: return 0
        
        if (!useSlope) return index
        
        val slope = (course.slope ?: 113).toDouble()
        val rating = course.rating ?: course.holes.sumOf { it.par }.toDouble()
        val par = course.holes.sumOf { it.par }.toDouble()
        
        // CH = Handicap Index * (Slope Rating / 113) + (Course Rating - Par)
        val ch = index.toDouble() * (slope / 113.0) + (rating - par)
        return ch.roundToInt()
    }
    
    fun strokesReceived(golferHandicap: Int, holeHandicap: Int): Int {
        var strokes = 0
        val absHandicap = kotlin.math.abs(golferHandicap)
        
        // Base strokes per hole if the handicap is > 18
        strokes += absHandicap / 18
        
        // Remaining strokes allocated by hole difficulty
        val remainder = absHandicap % 18
        if (holeHandicap <= remainder) {
            strokes += 1
        }
        
        // If the golfer has a plus handicap, they GIVE strokes back on the easiest holes
        if (golferHandicap < 0) {
            val giveBack = absHandicap
            val invertedHoleHcp = 19 - holeHandicap
            
            strokes = 0 // cancel base strokes
            strokes -= giveBack / 18
            val giveBackRemainder = giveBack % 18
            if (invertedHoleHcp <= giveBackRemainder) {
                strokes -= 1
            }
        }
        
        return strokes
    }
}
