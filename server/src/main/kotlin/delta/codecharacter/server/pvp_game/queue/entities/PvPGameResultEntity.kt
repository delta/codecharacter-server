package delta.codecharacter.server.pvp_game.queue.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class PvPGameResultEntity (
    @field:JsonProperty("score", required = true) val score: Int,
    @field:JsonProperty("has_errors", required = true) val hasErrors: Boolean,
    @field:JsonProperty("log", required = true) val log: String,
)
