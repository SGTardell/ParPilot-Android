package com.stardell.parpilotai.models

import java.util.UUID

data class Course(
    val id: UUID = UUID.randomUUID(),
    var name: String,
    var holes: List<Hole>,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var slope: Int? = 113,
    var rating: Double? = null
) {
    companion object {
        fun createStandard(name: String): Course {
            val standardHoles = (1..18).map { i ->
                val par = if (i == 3 || i == 11) 3 else if (i == 6 || i == 15) 5 else 4
                val yardage = if (par == 3) 150 else if (par == 5) 500 else 350
                val hcp = if (i % 2 != 0) i else 19 - i
                Hole(number = i, par = par, yardage = yardage, handicap = hcp)
            }
            return Course(name = name, holes = standardHoles)
        }
    }
}
