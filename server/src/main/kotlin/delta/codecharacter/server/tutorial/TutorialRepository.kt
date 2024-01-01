package delta.codecharacter.server.tutorial

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface TutorialRepository : MongoRepository<TutorialEntity, UUID> {
    fun findByNumber(number: Int): Optional<TutorialEntity>
}
