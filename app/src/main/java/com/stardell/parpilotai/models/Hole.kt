package com.stardell.parpilotai.models

import java.util.UUID

data class Hole(
    val id: UUID = UUID.randomUUID(),
    var number: Int,
    var par: Int,
    var yardage: Int,
    var handicap: Int,
    var greenLatitude: Double? = null,
    var greenLongitude: Double? = null
)
