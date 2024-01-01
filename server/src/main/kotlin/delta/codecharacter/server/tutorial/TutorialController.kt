package delta.codecharacter.server.tutorial

import delta.codecharacter.core.TutorialsApi
import delta.codecharacter.dtos.TutorialsGetRequestDto
import delta.codecharacter.server.user.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class TutorialController(
        @Autowired private val tutorialService: TutorialService,
) : TutorialsApi {
    @Secured(value = ["ROLE_USER"])
    override fun getTutorials(): ResponseEntity<TutorialsGetRequestDto> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        return ResponseEntity.ok(tutorialService.getTutorialByNumberForUser(user.id))
    }
}
