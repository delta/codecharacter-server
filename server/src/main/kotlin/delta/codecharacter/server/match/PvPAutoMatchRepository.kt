package delta.codecharacter.server.match

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository interface PvPAutoMatchRepository : MongoRepository<PvPAutoMatchEntity, UUID>
