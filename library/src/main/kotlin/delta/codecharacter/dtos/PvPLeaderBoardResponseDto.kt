package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.dtos.PublicUserDto
import delta.codecharacter.dtos.PvPUserStatsDto
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
 * Response model for PvP leaderboard
 * @param user 
 * @param stats 
 */
data class PvPLeaderBoardResponseDto(

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("user", required = true) val user: PublicUserDto,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("stats", required = true) val stats: PvPUserStatsDto
) {

}

