package delta.codecharacter.server.pvp_game

import delta.codecharacter.core.PvpGameApi
import delta.codecharacter.server.pvp_game.pvp_game_log.PvPGameLogService
import delta.codecharacter.server.user.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class PvPGameController(@Autowired private val pvPGameLogService: PvPGameLogService): PvpGameApi {
    @Secured(value = ["ROLE_USER"])
    override fun getPvpGameLogsByGameId(gameId: UUID): ResponseEntity<String> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        val userId = user.id
        return ResponseEntity.ok(pvPGameLogService.getPlayerLog(gameId, userId))
    }
}
