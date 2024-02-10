package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

/**
 * User Match Stat model
 * @param avgAtk
 * @param dcWins
 * @param coins
 */
data class UserMatchStatDto(

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("avgAtk", required = true) val avgAtk: java.math.BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("dc_wins", required = true) val dcWins: java.math.BigDecimal = BigDecimal.ZERO,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("coins", required = true) val coins: java.math.BigDecimal = BigDecimal.ZERO
) {

}

