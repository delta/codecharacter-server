
package delta.codecharacter.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

/**
 * User Match Stat model
 * @param maxAtk
 * @param minAtk
 * @param avgAtk
 * @param dcWins
 * @param dcLosses
 * @param dcCompletions
 * @param dcDestruction
 * @param coins
 */
data class UserMatchStatsInnerDto(

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("maxAtk", required = true) val maxAtk: BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("minAtk", required = true) val minAtk: BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("avgAtk", required = true) val avgAtk: BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("dc_wins", required = true) val dcWins: BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("dc_losses", required = true) val dcLosses: BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("dc_completions", required = true) val dcCompletions: BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("dc_destruction", required = true) val dcDestruction: BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("coins", required = true) val coins: BigDecimal = BigDecimal.ZERO
)

