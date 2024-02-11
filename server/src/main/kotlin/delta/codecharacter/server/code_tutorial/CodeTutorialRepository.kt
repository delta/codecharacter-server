package delta.codecharacter.server.code_tutorial

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface CodeTutorialRepository : MongoRepository<CodeTutorialEntity, UUID> {
    fun findByNumber(number: Int): Optional<CodeTutorialEntity>
}
