package delta.codecharacter.server.pvp_game.queue.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class PvPGameResultEntity (
    @field:JsonProperty("destruction_percentage", required = true) val destructionPercentage: Double,
    @field:JsonProperty("coins_used", required = true) val coinsUsed: Int,
    @field:JsonProperty("has_errors", required = true) val hasErrors: Boolean,
    @field:JsonProperty("log", required = true) val log: String,
)
