package delta.codecharacter.server.pvp_game.pvp_game_log

import delta.codecharacter.server.match.MatchRepository
import delta.codecharacter.server.match.PvPMatchRepository
import delta.codecharacter.server.user.public_user.PublicUserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PvPGameLogService(
    @Autowired private val pvPGameLogRepository: PvPGameLogRepository,
    @Autowired private val pvPMatchRepository: PvPMatchRepository,
) {

    fun getPlayerLog(gameId: UUID, userId: UUID): String {
        val match = pvPMatchRepository.findById(gameId)

        if (!match.isPresent) {
            return ""
        }

        val pvPGameLog = pvPGameLogRepository.findById(gameId)
        val player1 : PublicUserEntity = match.get().player1
        val player2 : PublicUserEntity = match.get().player2

        return if (pvPGameLog.isPresent) {
            if (player1.userId == userId) {
                pvPGameLog.get().player1Log
            } else if (player2.userId == userId) {
                pvPGameLog.get().player2Log
            } else {
                ""
            }
        } else {
            ""
        }
    }

    fun savePvPGameLog(gameId: UUID, player1Log: String, player2Log: String) {
        val pvPGameLog = PvPGameLogEntity(gameId, player1Log, player2Log)
        pvPGameLogRepository.save(pvPGameLog)
    }
}
