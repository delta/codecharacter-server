package delta.codecharacter.server.stats

import org.springframework.data.mongodb.core.mapping.Document

data class StatEntity (
    val maxAtk :Double,
    val minAtk : Double,
    val avgAtk : Double,
    val dc_wins : Int,
    val dc_losses : Int,
    val dc_completions: Int,
    val dc_destruction: Int,
    val coins : Int
){
}
