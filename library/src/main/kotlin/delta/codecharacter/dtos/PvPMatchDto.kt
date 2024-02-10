package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import delta.codecharacter.dtos.MatchModeDto
import delta.codecharacter.dtos.PublicUserDto
import delta.codecharacter.dtos.PvPGameDto
import delta.codecharacter.dtos.VerdictDto
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
 * PvP Match model
 * @param id 
 * @param game 
 * @param matchMode 
 * @param matchVerdict 
 * @param createdAt 
 * @param user1 
 * @param user2 
 */
data class PvPMatchDto(

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", required = true, description = "")
    @get:JsonProperty("id", required = true) val id: java.util.UUID,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("game", required = true) val game: PvPGameDto,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("matchMode", required = true) val matchMode: MatchModeDto,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("matchVerdict", required = true) val matchVerdict: VerdictDto,

    @Schema(example = "2021-01-01T00:00Z", required = true, description = "")
    @get:JsonProperty("createdAt", required = true) val createdAt: java.time.Instant,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("user1", required = true) val user1: PublicUserDto,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("user2", required = true) val user2: PublicUserDto
) {

}

