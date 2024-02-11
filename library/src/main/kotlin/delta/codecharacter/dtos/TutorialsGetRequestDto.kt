package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.TutorialCodeObjectDto
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
 * Get the game tutorials
 * @param tutorialId 
 * @param tutorialName 
 * @param tutorialCodes 
 * @param tutorialType 
 * @param description 
 */
data class TutorialsGetRequestDto(

    @Schema(example = "1", required = true, description = "")
    @get:JsonProperty("tutorialId", required = true) val tutorialId: kotlin.Int,

    @Schema(example = "Tutorial for Spawning attacker", required = true, description = "")
    @get:JsonProperty("tutorialName", required = true) val tutorialName: kotlin.String,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("tutorialCodes", required = true) val tutorialCodes: TutorialCodeObjectDto,

    @field:Valid
    @Schema(example = "null", description = "")
    @get:JsonProperty("tutorialType") val tutorialType: ChallengeTypeDto? = null,

    @Schema(example = "Brief description of how the code works", description = "")
    @get:JsonProperty("description") val description: kotlin.String? = null
) {

}

