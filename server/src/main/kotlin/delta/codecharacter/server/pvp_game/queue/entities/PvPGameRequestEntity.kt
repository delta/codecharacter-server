package delta.codecharacter.server.pvp_game.queue.entities

import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.server.params.GameCode
import delta.codecharacter.server.params.GameParameters
import java.util.UUID

data class PvPGameRequestEntity (
    @field:JsonProperty("game_id", required = true) val gameId: UUID,
    @field:JsonProperty("parameters", required = true) val parameters: GameParameters,
    @field:JsonProperty("player1", required = true) val player1: GameCode,
    @field:JsonProperty("player2", required = true) val player2: GameCode
)
