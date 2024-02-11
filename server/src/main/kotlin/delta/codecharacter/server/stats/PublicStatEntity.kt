package delta.codecharacter.server.stats

import delta.codecharacter.server.user.public_user.DailyChallengeHistory
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection="stat")
data class PublicStatEntity(
    @Id val userId: UUID,
    var stats: HashMap<Int, StatEntity>
)
