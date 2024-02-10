package delta.codecharacter.server.match

import delta.codecharacter.server.pvp_game.PvPGameEntity
import delta.codecharacter.server.user.public_user.PublicUserEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DocumentReference
import java.time.Instant
import java.util.UUID

@Document(collection = "pvp_match")
data class PvPMatchEntity (
    @Id val id: UUID,
    @DocumentReference(lazy = true) val game: PvPGameEntity,
    val mode: MatchModeEnum,
    val verdict: MatchVerdictEnum,
    val createdAt: Instant,
    val totalPoints: Int,
    @DocumentReference(lazy = true) val player1: PublicUserEntity,
    @DocumentReference(lazy = true) val player2: PublicUserEntity,
)
