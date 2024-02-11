package delta.codecharacter.server.stats

import delta.codecharacter.core.NotificationApi
import delta.codecharacter.core.StatsApi
import delta.codecharacter.dtos.DailyChallengeGetRequestDto
import delta.codecharacter.dtos.UserMatchStatsInnerDto
import delta.codecharacter.server.notifications.NotificationService
import delta.codecharacter.server.user.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class StatsController(@Autowired private val statsService: StatsService) : StatsApi {

    @Secured(value = ["ROLE_USER"])
    override fun getStats(): ResponseEntity<List<List<UserMatchStatsInnerDto>>> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        return ResponseEntity.ok(statsService.getInfo(user.id))
    }
}
