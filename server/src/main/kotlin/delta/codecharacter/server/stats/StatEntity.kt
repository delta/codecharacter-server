package delta.codecharacter.server.stats

import delta.codecharacter.server.match.MatchModeEnum
import java.time.Instant

data class StatEntity (
    val avgAtk : Double,
    val dc_wins : Int,
    val coins : Int,
    val createdAt: Instant,
)
