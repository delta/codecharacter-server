package delta.codecharacter.server.pvp_game

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document(collection = "pvp_game")
data class PvPGameEntity (
    @Id val matchId: UUID,
    var destructionPlayer1: Double,
    var destructionPlayer2: Double,
    var coinsUsedPlayer1: Int,
    var coinsUsedPlayer2: Int,
    var status: PvPGameStatusEnum
)
