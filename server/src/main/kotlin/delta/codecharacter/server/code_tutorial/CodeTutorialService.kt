package delta.codecharacter.server.code_tutorial

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.TutorialsGetRequestDto
import delta.codecharacter.dtos.UpdateCurrentUserProfileDto
import delta.codecharacter.server.code_tutorial.match.CodeTutorialMatchVerdictEnum
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.game.GameEntity
import delta.codecharacter.server.game.GameStatusEnum
import delta.codecharacter.server.user.public_user.PublicUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.properties.Delegates

@Service
class CodeTutorialService(
        @Autowired private val codeTutorialRepository: CodeTutorialRepository,
        @Autowired private val publicUserService: PublicUserService,
) {

    @Value("\${environment.event-start-date}") private lateinit var startDate: String
    private var currentTutorialNumber = 1
    fun getTutorialByNumber(tutorialNumber: Int): CodeTutorialEntity {
        val currentTutorial =
            codeTutorialRepository.findByNumber(tutorialNumber).orElseThrow {
                throw CustomException(HttpStatus.BAD_REQUEST, "Invalid Request")
            }
        currentTutorialNumber = tutorialNumber
        return currentTutorial
    }

    fun getTutorialByNumberForUser(userId: UUID, codeTutorialNumber: Int): TutorialsGetRequestDto {
        val maxCodeTutorialNumber = publicUserService.getPublicUser(userId).codeTutorialLevel
        lateinit var currentTutorial: CodeTutorialEntity
        if(codeTutorialNumber <= maxCodeTutorialNumber)
        {
            currentTutorial = getTutorialByNumber(codeTutorialNumber)
        }
        else
        {
            throw CustomException(HttpStatus.BAD_REQUEST, "Complete the current tutorial first")
        }
        return TutorialsGetRequestDto(
                tutorialName = currentTutorial.tutName,
                tutorialCodes = currentTutorial.tutorial,
                tutorialType = currentTutorial.tutType,
                description = currentTutorial.description,
        )
    }
    fun completeCodeTutorial(gameEntity: GameEntity, userId: UUID): CodeTutorialMatchVerdictEnum {
        val destruction = gameEntity.destruction
        if (gameEntity.status == GameStatusEnum.EXECUTE_ERROR)
            return CodeTutorialMatchVerdictEnum.FAILURE
        val currentCodeTutorial = getTutorialByNumber(currentTutorialNumber)
        if ((
                        currentCodeTutorial.tutType == ChallengeTypeDto.MAP &&
                                destruction >= 75.0
                        ) ||
                (
                        currentCodeTutorial.tutType == ChallengeTypeDto.CODE &&
                                destruction < 75.0
                        )
        ) {
            publicUserService.updateUserCodeTutorialLevel(userId, true)
            return CodeTutorialMatchVerdictEnum.SUCCESS
        }
        return CodeTutorialMatchVerdictEnum.FAILURE
    }
}
