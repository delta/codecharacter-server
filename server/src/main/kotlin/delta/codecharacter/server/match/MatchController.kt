package delta.codecharacter.server.match

import delta.codecharacter.core.MatchApi
import delta.codecharacter.dtos.CreateMatchRequestDto
import delta.codecharacter.dtos.MatchDto
import delta.codecharacter.dtos.MatchModeDto
import delta.codecharacter.dtos.PvPMatchDto
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.user.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class MatchController(@Autowired private val matchService: MatchService) : MatchApi {

    @Secured("ROLE_USER")
    override fun createMatch(createMatchRequestDto: CreateMatchRequestDto): ResponseEntity<Unit> {
        if (createMatchRequestDto.mode == MatchModeDto.AUTO) {
            throw CustomException(HttpStatus.BAD_REQUEST, "Users cannot create auto match")
        }
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        matchService.createMatch(user.id, createMatchRequestDto)
        return ResponseEntity.ok().build()
    }

    @Secured("ROLE_USER")
    override fun getTopMatches(): ResponseEntity<List<Any>> {
        return ResponseEntity.ok(matchService.getTopMatches())
    }

    @Secured("ROLE_USER")
    override fun getUserNormalMatches(
        page: Int?,
        size: Int?,
    ): ResponseEntity<List<MatchDto>> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        return ResponseEntity.ok(matchService.getUserNormalMatches(user.id, page, size))
    }

    @Secured("ROLE_USER")
    override fun getUserPvPMatches(
        page: Int?,
        size: Int?,
    ): ResponseEntity<List<PvPMatchDto>> {
        val user = SecurityContextHolder.getContext().authentication.principal as UserEntity
        return ResponseEntity.ok(matchService.getUserPvPMatches(user.id, page, size))
    }
}
