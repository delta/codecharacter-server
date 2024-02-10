package delta.codecharacter.server.game.queue.entities

import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.server.params.GameCode
import delta.codecharacter.server.params.GameParameters
import java.util.UUID

data class GameRequestEntity(
    @field:JsonProperty("game_id", required = true) val gameId: UUID,
    @field:JsonProperty("parameters", required = true) val parameters: GameParameters,
    @field:JsonProperty("player_code", required = true) val playerCode: GameCode,
    @field:JsonProperty("map", required = true) val map: String,
)
