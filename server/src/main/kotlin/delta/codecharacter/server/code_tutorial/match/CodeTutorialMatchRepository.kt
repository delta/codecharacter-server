package delta.codecharacter.server.code_tutorial.match

import delta.codecharacter.server.user.public_user.PublicUserEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CodeTutorialMatchRepository : MongoRepository<CodeTutorialMatchEntity, UUID> {
    fun findByUserOrderByCreatedAtDesc(user: PublicUserEntity): List<CodeTutorialMatchEntity>
}
