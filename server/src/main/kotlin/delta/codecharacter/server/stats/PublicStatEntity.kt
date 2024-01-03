package delta.codecharacter.server.stats

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection="stats")
data class PublicStatEntity(
    @Id val userId: UUID,
    val stats : List<StatEntity>
)
