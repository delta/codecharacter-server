package delta.codecharacter.server.stats

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StatsRepository : MongoRepository<PublicStatEntity, UUID>
