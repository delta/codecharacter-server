package delta.codecharacter.server.match

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document(collection = "pvp_auto_match")
data class PvPAutoMatchEntity(@Id val matchId: UUID, val tries: Int)
