package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import delta.codecharacter.dtos.PvPGameStatusDto
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
 * PvP Game model
 * @param id 
 * @param scorePlayer1 
 * @param scorePlayer2 
 * @param status 
 */
data class PvPGameDto(

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", required = true, description = "")
    @get:JsonProperty("id", required = true) val id: java.util.UUID,

    @Schema(example = "69", required = true, description = "")
    @get:JsonProperty("scorePlayer1", required = true) val scorePlayer1: kotlin.Int,

    @Schema(example = "69", required = true, description = "")
    @get:JsonProperty("scorePlayer2", required = true) val scorePlayer2: kotlin.Int,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("status", required = true) val status: PvPGameStatusDto
) {

}

