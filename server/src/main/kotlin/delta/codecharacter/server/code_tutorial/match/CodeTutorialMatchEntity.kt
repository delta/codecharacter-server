package delta.codecharacter.server.code_tutorial.match

import delta.codecharacter.server.game.GameEntity
import delta.codecharacter.server.user.public_user.PublicUserEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DocumentReference
import java.time.Instant
import java.util.UUID

@Document(collection = "code_tutorial_match")
data class CodeTutorialMatchEntity(
        @Id val id: UUID,
        val number: Int,
        @DocumentReference(lazy = true) val game: GameEntity,
        val verdict: CodeTutorialMatchVerdictEnum,
        @DocumentReference(lazy = true) val user: PublicUserEntity,
        val createdAt: Instant
)
