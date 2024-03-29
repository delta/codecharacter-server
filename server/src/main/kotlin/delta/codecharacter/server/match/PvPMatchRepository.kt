package delta.codecharacter.server.match

import delta.codecharacter.server.user.public_user.PublicUserEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PvPMatchRepository : MongoRepository<PvPMatchEntity, UUID> {
    fun findTop10ByOrderByTotalPointsDesc(): List<PvPMatchEntity>
    fun findByPlayer1OrderByCreatedAtDesc(player1: PublicUserEntity, pageRequest: PageRequest): List<PvPMatchEntity>
    fun findByIdIn(matchIds: List<UUID>): List<PvPMatchEntity>
}
