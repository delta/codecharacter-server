package delta.codecharacter.server.user.rating_history

import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

interface RatingHistoryRepository : MongoRepository<RatingHistoryEntity, String> {
    fun findFirstByUserIdAndRatingTypeOrderByValidFromDesc(userId: UUID,ratingType: RatingType): RatingHistoryEntity
    fun findAllByUserIdAndRatingType(userId: UUID,ratingType: RatingType): List<RatingHistoryEntity>
}
