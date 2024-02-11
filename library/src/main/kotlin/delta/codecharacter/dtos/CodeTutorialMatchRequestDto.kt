package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import delta.codecharacter.dtos.LanguageDto
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
 * Request Model for the tutorial mode
 * @param &#x60;value&#x60; 
 * @param codeTutorialNumber 
 * @param language 
 */
data class CodeTutorialMatchRequestDto(

    @Schema(example = "#include<iostream>", required = true, description = "")
    @get:JsonProperty("value", required = true) val `value`: kotlin.String,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("codeTutorialNumber", required = true) val codeTutorialNumber: kotlin.Int,

    @field:Valid
    @Schema(example = "null", description = "")
    @get:JsonProperty("language") val language: LanguageDto? = null
) {

}

