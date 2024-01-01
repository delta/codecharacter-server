package delta.codecharacter.server.tutorial

import delta.codecharacter.dtos.TutorialsGetRequestDto
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.user.public_user.PublicUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TutorialService(
        @Autowired private val tutorialRepository: TutorialRepository,
        @Autowired private val publicUserService: PublicUserService
) {

    fun getTutorialByNumber(): TutorialEntity {
        val currentTutorial =
            tutorialRepository.findByNumber(1).orElseThrow {
                throw CustomException(HttpStatus.BAD_REQUEST, "Invalid Request")
            }
        return currentTutorial
    }

    fun getTutorialByNumberForUser(userId: UUID): TutorialsGetRequestDto {
        val user = publicUserService.getPublicUser(userId)
        val currentTutorial = getTutorialByNumber()
        return TutorialsGetRequestDto(
            tutorialName = currentTutorial.tutName,
            tutorialCodes = currentTutorial.tutorial,
            tutorialType = currentTutorial.tutType,
            description = currentTutorial.description,
        )
    }
}
