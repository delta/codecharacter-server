package delta.codecharacter.server.code_tutorial

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.TutorialsGetRequestDto
import delta.codecharacter.dtos.UpdateCurrentUserProfileDto
import delta.codecharacter.server.code_tutorial.match.CodeTutorialMatchVerdictEnum
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.game.GameEntity
import delta.codecharacter.server.game.GameStatusEnum
import delta.codecharacter.server.notifications.NotificationService
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
    @Autowired private val notificationService: NotificationService,
    @Autowired private val publicUserService: PublicUserService,
) {

    @Value("\${environment.event-start-date}") private lateinit var startDate: String
    @Value("\${environment.total-no-of-code-tutorial-levels}") private var totalCodeTutorials: Int = 4
    fun getTutorialByNumber(tutorialNumber: Int): CodeTutorialEntity {
        val currentTutorial =
            codeTutorialRepository.findByNumber(tutorialNumber).orElseThrow {
                throw CustomException(HttpStatus.BAD_REQUEST, "Invalid Request")
            }
        return currentTutorial
    }

    fun getTutorialByNumberForUser(userId: UUID, codeTutorialNumber: Int): TutorialsGetRequestDto {
        val maxCodeTutorialNumber = publicUserService.getPublicUser(userId).codeTutorialLevel
        lateinit var currentTutorial: CodeTutorialEntity
        if(codeTutorialNumber <= maxCodeTutorialNumber)
        {
            currentTutorial = getTutorialByNumber(codeTutorialNumber)
        }
        else if(codeTutorialNumber <= totalCodeTutorials)
        {
            throw CustomException(HttpStatus.BAD_REQUEST, "Complete the current tutorial first")
        }
        else
        {
            throw CustomException(HttpStatus.SERVICE_UNAVAILABLE, "No more tutorials!")
        }
        return TutorialsGetRequestDto(
                tutorialName = currentTutorial.tutName,
                tutorialCodes = currentTutorial.tutorial,
                tutorialType = currentTutorial.tutType,
                description = currentTutorial.description,
                tutorialId = currentTutorial.number,
        )
    }
    fun completeCodeTutorial(gameEntity: GameEntity, userId: UUID, number: Int): CodeTutorialMatchVerdictEnum {
        val destruction = gameEntity.destruction
        if (gameEntity.status == GameStatusEnum.EXECUTE_ERROR)
            return CodeTutorialMatchVerdictEnum.FAILURE
        val currentCodeTutorial = getTutorialByNumber(number)
        if ((
                        currentCodeTutorial.tutType == ChallengeTypeDto.MAP &&
                                destruction >= 75.0
                        ) ||
                (
                        currentCodeTutorial.tutType == ChallengeTypeDto.CODE &&
                                destruction < 75.0
                        )
        ) {
            if(number == publicUserService.getPublicUser(userId).codeTutorialLevel)
            {
                publicUserService.updateUserCodeTutorialLevel(userId, true)
            }
            if(number == totalCodeTutorials)
            {
                notificationService.sendNotification(
                    userId,
                    title = "Completed all tutorials!",
                    content = ""
                )
            }
            return CodeTutorialMatchVerdictEnum.SUCCESS
        }
        return CodeTutorialMatchVerdictEnum.FAILURE
    }
}
