package delta.codecharacter.server.pvp_game.queue.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.server.pvp_game.PvPGameStatusEnum
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class PvPGameStatusUpdateEntity(
    @field:JsonProperty("game_id", required = true) val gameId: UUID,
    @field:JsonProperty("game_status", required = true) val gameStatus: PvPGameStatusEnum,
    @field:JsonProperty("game_result_player1", required = false) val gameResultPlayer1: PvPGameResultEntity?,
    @field:JsonProperty("game_result_player2", required = false) val gameResultPlayer2: PvPGameResultEntity?
)
