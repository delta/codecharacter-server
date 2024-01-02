package delta.codecharacter.server.tutorial

import delta.codecharacter.dtos.TutorialsGetRequestDto
import delta.codecharacter.server.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TutorialService(
        @Autowired private val tutorialRepository: TutorialRepository,
) {

    fun getTutorialByNumber(tutorialNumber: Int): TutorialEntity {
        val currentTutorial =
            tutorialRepository.findByNumber(tutorialNumber).orElseThrow {
                throw CustomException(HttpStatus.BAD_REQUEST, "Invalid Request")
            }
        return currentTutorial
    }

    fun getTutorialByNumberForUser(userId: UUID, tutorialNumber: Int): TutorialsGetRequestDto {
        val currentTutorial = getTutorialByNumber(tutorialNumber)
        return TutorialsGetRequestDto(
            tutorialName = currentTutorial.tutName,
            tutorialCodes = currentTutorial.tutorial,
            tutorialType = currentTutorial.tutType,
            description = currentTutorial.description,
        )
    }
}
