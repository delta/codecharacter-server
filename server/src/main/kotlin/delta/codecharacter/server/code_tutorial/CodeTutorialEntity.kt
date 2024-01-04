package delta.codecharacter.server.code_tutorial

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.TutorialCodeObjectDto
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document(collection = "tutorials")
data class CodeTutorialEntity(
    @Id val id: UUID,
    val number: Int,
    val tutName: String,
    val tutType: ChallengeTypeDto,
    val tutorial: TutorialCodeObjectDto,
    val description: String?,
    val map: String
)
