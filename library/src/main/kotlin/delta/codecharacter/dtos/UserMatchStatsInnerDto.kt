package delta.codecharacter.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param avgAtk
 * @param dcWins
 * @param coins
 */
data class UserMatchStatsInnerDto(

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("avgAtk", required = true) val avgAtk: java.math.BigDecimal,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("dc_wins", required = true) val dcWins: java.math.BigDecimal,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("coins", required = true) val coins: java.math.BigDecimal
) {

}

