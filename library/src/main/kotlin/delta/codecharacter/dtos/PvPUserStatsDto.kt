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

/**
 * PvP User stats model
 * @param rating 
 * @param wins 
 * @param losses 
 */
data class PvPUserStatsDto(

    @Schema(example = "1000", required = true, description = "")
    @get:JsonProperty("rating", required = true) val rating: java.math.BigDecimal,

    @Schema(example = "1", required = true, description = "")
    @get:JsonProperty("wins", required = true) val wins: kotlin.Int = 0,

    @Schema(example = "1", required = true, description = "")
    @get:JsonProperty("losses", required = true) val losses: kotlin.Int
) {

}

