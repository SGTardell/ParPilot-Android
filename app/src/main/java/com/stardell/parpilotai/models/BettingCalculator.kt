package com.stardell.parpilotai.models

import java.util.UUID
import kotlin.math.abs

data class Transfer(
    val id: UUID = UUID.randomUUID(),
    val fromId: UUID,
    val fromName: String,
    val toId: UUID,
    val toName: String,
    val toVenmo: String,
    val amount: Double
)

data class SettlementResult(
    val netPayouts: Map<UUID, Double>,
    val transfers: List<Transfer>
)

object BettingCalculator {

    fun calculateSettlement(
        scorecard: Scorecard,
        course: Course,
        golfers: List<Golfer>
    ): SettlementResult {
        val netPayouts = mutableMapOf<UUID, Double>()
        scorecard.golferIds.forEach { netPayouts[it] = 0.0 }

        val games = scorecard.bettingGames ?: emptyList()
        if (games.isEmpty()) {
            return SettlementResult(netPayouts, emptyList())
        }

        for (game in games) {
            when (game.gameType) {
                GameType.SKINS -> {
                    val skinsWon = calculateSkins(scorecard, course, golfers, game.wagerAmount, game.useNetScore, game.skinsCarryOver)
                    val payouts = calculateSkinsPayout(skinsWon, game.wagerAmount, scorecard.golferIds.size)
                    payouts.forEach { (id, valAmt) -> netPayouts[id] = (netPayouts[id] ?: 0.0) + valAmt }
                }
                GameType.NASSAU -> {
                    // TODO: Full Nassau Calculation
                }
                GameType.MATCH_PLAY -> {
                    // TODO: Full Match Play Calculation
                }
                GameType.STABLEFORD -> {
                    val points = calculateStableford(scorecard, course, golfers, game.useNetScore, game.stablefordType)
                    val payouts = calculateStablefordPayout(points, game.wagerAmount)
                    payouts.forEach { (id, valAmt) -> netPayouts[id] = (netPayouts[id] ?: 0.0) + valAmt }
                }
                GameType.CLOSEST_TO_PIN -> {
                    val payouts = calculateClosestToPin(scorecard, game.wagerAmount, game.closestToPinWinners)
                    payouts.forEach { (id, valAmt) -> netPayouts[id] = (netPayouts[id] ?: 0.0) + valAmt }
                }
                GameType.WOLF -> {
                    // TODO: Full Wolf Calculation
                }
                GameType.BIRDIES_AND_EAGLES -> {
                    val payouts = calculateBirdiesAndEagles(scorecard, course, game.birdieWager, game.eagleWager)
                    payouts.forEach { (id, valAmt) -> netPayouts[id] = (netPayouts[id] ?: 0.0) + valAmt }
                }
            }
        }

        // Greedy match solver for settling debts
        val debtors = mutableListOf<Pair<UUID, Double>>()
        val creditors = mutableListOf<Pair<UUID, Double>>()

        for ((id, amount) in netPayouts) {
            if (amount < -0.01) {
                debtors.add(Pair(id, abs(amount)))
            } else if (amount > 0.01) {
                creditors.add(Pair(id, amount))
            }
        }

        val transfers = mutableListOf<Transfer>()
        var dIdx = 0
        var cIdx = 0

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val (dId, dAmt) = debtors[dIdx]
            val (cId, cAmt) = creditors[cIdx]

            val settleAmount = minOf(dAmt, cAmt)

            val debtorGolfer = golfers.find { it.id == dId }
            val creditorGolfer = golfers.find { it.id == cId }

            transfers.add(
                Transfer(
                    fromId = dId,
                    fromName = debtorGolfer?.name ?: "Unknown",
                    toId = cId,
                    toName = creditorGolfer?.name ?: "Unknown",
                    toVenmo = creditorGolfer?.venmo ?: "",
                    amount = settleAmount
                )
            )

            debtors[dIdx] = debtors[dIdx].copy(second = dAmt - settleAmount)
            creditors[cIdx] = creditors[cIdx].copy(second = cAmt - settleAmount)

            if (debtors[dIdx].second < 0.01) dIdx++
            if (creditors[cIdx].second < 0.01) cIdx++
        }

        return SettlementResult(netPayouts, transfers)
    }

    // MARK: - Skins
    private fun calculateSkins(
        scorecard: Scorecard, course: Course, golfers: List<Golfer>,
        wager: Double, useNet: Boolean, carryOver: Boolean
    ): Map<UUID, Int> {
        val skinsWon = mutableMapOf<UUID, Int>()
        scorecard.golferIds.forEach { skinsWon[it] = 0 }
        var currentCarry = 0
        val sortedHoles = course.holes.sortedBy { it.number }

        for (hole in sortedHoles) {
            val scoresForHole = mutableListOf<Pair<UUID, Int>>()
            var allScored = true
            for (gId in scorecard.golferIds) {
                val gross = scorecard.scores["${gId}_${hole.number}"]
                if (gross != null) {
                    val scoreToUse = if (useNet) {
                        val golfer = golfers.find { it.id == gId }
                        val hcp = golfer?.let { scorecard.courseHandicap(it, course) } ?: 0
                        gross - scorecard.strokesReceived(hcp, hole.handicap)
                    } else {
                        gross
                    }
                    scoresForHole.add(Pair(gId, scoreToUse))
                } else {
                    allScored = false
                    break
                }
            }
            if (!allScored) continue
            val minScore = scoresForHole.minByOrNull { it.second }?.second ?: continue
            val playersWithMin = scoresForHole.filter { it.second == minScore }

            if (playersWithMin.size == 1) {
                val winnerId = playersWithMin[0].first
                skinsWon[winnerId] = (skinsWon[winnerId] ?: 0) + 1 + currentCarry
                currentCarry = 0
            } else {
                if (carryOver) currentCarry += 1
            }
        }
        return skinsWon
    }

    private fun calculateSkinsPayout(skinsWon: Map<UUID, Int>, wager: Double, golfersCount: Int): Map<UUID, Double> {
        val payouts = mutableMapOf<UUID, Double>()
        val totalSkinsWon = skinsWon.values.sum()
        for ((golferId, skins) in skinsWon) {
            val wonFromOthers = skins * wager * (golfersCount - 1)
            val lostToOthers = (totalSkinsWon - skins) * wager
            payouts[golferId] = wonFromOthers - lostToOthers
        }
        return payouts
    }

    // MARK: - Stableford
    private fun calculateStableford(
        scorecard: Scorecard, course: Course, golfers: List<Golfer>,
        useNet: Boolean, type: StablefordType
    ): Map<UUID, Int> {
        val points = mutableMapOf<UUID, Int>()
        scorecard.golferIds.forEach { points[it] = 0 }
        for (hole in course.holes) {
            for (gId in scorecard.golferIds) {
                val gross = scorecard.scores["${gId}_${hole.number}"] ?: continue
                val scoreToUse = if (useNet) {
                    val golfer = golfers.find { it.id == gId }
                    val hcp = golfer?.let { scorecard.courseHandicap(it, course) } ?: 0
                    gross - scorecard.strokesReceived(hcp, hole.handicap)
                } else {
                    gross
                }
                val diff = scoreToUse - hole.par
                val pts = when (type) {
                    StablefordType.STANDARD -> {
                        when {
                            diff <= -3 -> 5
                            diff == -2 -> 4
                            diff == -1 -> 3
                            diff == 0 -> 2
                            diff == 1 -> 1
                            else -> 0
                        }
                    }
                    StablefordType.MODIFIED -> {
                        when {
                            diff <= -3 -> 8
                            diff == -2 -> 5
                            diff == -1 -> 2
                            diff == 0 -> 0
                            diff == 1 -> -1
                            else -> -3
                        }
                    }
                }
                points[gId] = (points[gId] ?: 0) + pts
            }
        }
        return points
    }

    private fun calculateStablefordPayout(points: Map<UUID, Int>, wager: Double): Map<UUID, Double> {
        val payouts = mutableMapOf<UUID, Double>()
        val n = points.size
        if (n <= 1) return payouts
        val totalPoints = points.values.sum()
        for ((golferId, pts) in points) {
            val wonFromOthers = (pts * (n - 1)) * wager
            val lostToOthers = (totalPoints - pts) * wager
            payouts[golferId] = wonFromOthers - lostToOthers
        }
        return payouts
    }

    // MARK: - Closest to Pin
    private fun calculateClosestToPin(scorecard: Scorecard, wager: Double, winners: Map<Int, UUID>): Map<UUID, Double> {
        val payouts = mutableMapOf<UUID, Double>()
        scorecard.golferIds.forEach { payouts[it] = 0.0 }
        val n = scorecard.golferIds.size
        if (n <= 1) return payouts

        for ((_, winnerId) in winners) {
            if (!scorecard.golferIds.contains(winnerId)) continue
            for (id in scorecard.golferIds) {
                if (id == winnerId) {
                    payouts[id] = (payouts[id] ?: 0.0) + (wager * (n - 1))
                } else {
                    payouts[id] = (payouts[id] ?: 0.0) - wager
                }
            }
        }
        return payouts
    }

    // MARK: - Birdies and Eagles
    private fun calculateBirdiesAndEagles(
        scorecard: Scorecard, course: Course,
        birdieWager: Double, eagleWager: Double
    ): Map<UUID, Double> {
        val payouts = mutableMapOf<UUID, Double>()
        val birdiesCount = mutableMapOf<UUID, Int>()
        val eaglesCount = mutableMapOf<UUID, Int>()
        scorecard.golferIds.forEach { 
            payouts[it] = 0.0
            birdiesCount[it] = 0
            eaglesCount[it] = 0
        }
        val n = scorecard.golferIds.size
        if (n <= 1) return payouts

        for (hole in course.holes) {
            for (gId in scorecard.golferIds) {
                val gross = scorecard.scores["${gId}_${hole.number}"] ?: continue
                val diff = gross - hole.par
                if (diff == -1) birdiesCount[gId] = (birdiesCount[gId] ?: 0) + 1
                else if (diff <= -2) eaglesCount[gId] = (eaglesCount[gId] ?: 0) + 1
            }
        }

        val totalBirdies = birdiesCount.values.sum()
        val totalEagles = eaglesCount.values.sum()

        for (gId in scorecard.golferIds) {
            val myBirdies = birdiesCount[gId] ?: 0
            val bPayout = ((myBirdies * (n - 1)) - (totalBirdies - myBirdies)) * birdieWager
            val myEagles = eaglesCount[gId] ?: 0
            val ePayout = ((myEagles * (n - 1)) - (totalEagles - myEagles)) * eagleWager
            payouts[gId] = bPayout + ePayout
        }
        return payouts
    }
}
