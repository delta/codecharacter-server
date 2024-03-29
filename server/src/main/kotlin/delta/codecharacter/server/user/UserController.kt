package delta.codecharacter.server.user

import delta.codecharacter.core.UserApi
import delta.codecharacter.dtos.ActivateUserRequestDto
import delta.codecharacter.dtos.RatingHistoryDto
import delta.codecharacter.dtos.RegisterUserRequestDto
import delta.codecharacter.server.user.rating_history.RatingHistoryService
import delta.codecharacter.server.user.rating_history.RatingType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class UserController(
    @Autowired private val userService: UserService,
    @Autowired private val ratingHistoryService: RatingHistoryService
) : UserApi {

    override fun register(
        @Validated registerUserRequestDto: RegisterUserRequestDto
    ): ResponseEntity<Unit> {
        userService.registerUser(registerUserRequestDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(Unit)
    }

    override fun activateUser(
        userId: UUID,
        activateUserRequestDto: ActivateUserRequestDto
    ): ResponseEntity<Unit> {
        userService.activateUser(userId, activateUserRequestDto.token)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Unit)
    }

    override fun getRatingHistory(userId: UUID): ResponseEntity<List<RatingHistoryDto>> {
        //EDIT AND TAKE AS PARAMETER
        val ratingHistory = ratingHistoryService.getRatingHistory(userId,RatingType.NORMAL)
        return ResponseEntity.ok(ratingHistory)
    }
}
