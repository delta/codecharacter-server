package delta.codecharacter.server.pvp_game.pvp_game_log

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DocumentReference
import java.util.UUID

@Document(collection = "pvp_game_log")
class PvPGameLogEntity (
    @Id val gameId: UUID,
    val player1Log: String,
    val player2Log: String,
)
