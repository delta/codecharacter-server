package delta.codecharacter.server.leaderboard

import delta.codecharacter.core.PvpLeaderboardApi
import delta.codecharacter.dtos.PvPLeaderBoardResponseDto
import delta.codecharacter.server.user.public_user.PublicUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity

@RestController
class PvPLeaderBoardController (@Autowired private val publicUserService: PublicUserService) :
    PvpLeaderboardApi {
        override fun getPvPLeaderboard(
            page: Int?,
            size: Int?,
        ): ResponseEntity<List<PvPLeaderBoardResponseDto>> {
         return ResponseEntity.ok(publicUserService.getPvPLeaderboard(page, size))
        }
    }
