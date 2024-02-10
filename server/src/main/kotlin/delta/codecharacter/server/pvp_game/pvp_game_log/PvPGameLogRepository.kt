package delta.codecharacter.server.pvp_game.pvp_game_log

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository interface PvPGameLogRepository : MongoRepository<PvPGameLogEntity, UUID>
