package delta.codecharacter.server.code_tutorial

import delta.codecharacter.core.TutorialsApi
import delta.codecharacter.dtos.CodeTutorialMatchRequestDto
import delta.codecharacter.dtos.DailyChallengeMatchRequestDto
import delta.codecharacter.dtos.TutorialsGetRequestDto
import delta.codecharacter.server.match.MatchService
import delta.codecharacter.server.user.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class CodeTutorialController(
        @Autowired private val codeTutorialService: CodeTutorialService,
        @Autowired private val matchService: MatchService
) : TutorialsApi {
    @Secured(value = ["ROLE_USER"])
    override fun getCodeTutorialByNumber(codeTutorialNumber: Int): ResponseEntity<TutorialsGetRequestDto> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        return ResponseEntity.ok(codeTutorialService.getTutorialByNumberForUser(user.id,codeTutorialNumber))
    }
    @Secured(value = ["ROLE_USER"])
    override fun createCodeTutorialMatch(
            codeTutorialMatchRequestDto: CodeTutorialMatchRequestDto
    ): ResponseEntity<Unit> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        return ResponseEntity.ok(matchService.createTutorialMatch(user.id, codeTutorialMatchRequestDto))
    }
}
