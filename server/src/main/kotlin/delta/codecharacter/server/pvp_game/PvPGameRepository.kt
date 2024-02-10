package delta.codecharacter.server.pvp_game

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository interface PvPGameRepository : MongoRepository<PvPGameEntity, UUID>
