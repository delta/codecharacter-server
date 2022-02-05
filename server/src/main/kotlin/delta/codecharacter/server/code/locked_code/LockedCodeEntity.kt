package delta.codecharacter.server.code.locked_code

import delta.codecharacter.server.code.LanguageEnum
import delta.codecharacter.server.user.UserEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DocumentReference

/**
 * Locked code entity.
 *
 * @param user
 * @param code
 * @param language
 */
@Document(collection = "locked_code")
data class LockedCodeEntity(
    @Id @DocumentReference(lazy = true) val user: UserEntity,
    val code: String,
    val language: LanguageEnum
)
