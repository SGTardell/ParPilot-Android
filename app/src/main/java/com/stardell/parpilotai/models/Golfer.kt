package com.stardell.parpilotai.models

import java.util.UUID

data class Golfer(
    val id: UUID = UUID.randomUUID(),
    var name: String = "",
    var handicap: Int? = null,
    var phone: String = "",
    var venmo: String = "",
    var email: String = "",
    var photoFileName: String? = null
)
