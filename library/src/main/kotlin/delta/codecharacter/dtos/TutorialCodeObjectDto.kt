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
 * The object containing the code for the tutorial
 * @param cpp 
 * @param java 
 * @param python 
 * @param image 
 */
data class TutorialCodeObjectDto(

    @Schema(example = "This would have the cpp code for the tutorial", description = "")
    @get:JsonProperty("cpp") val cpp: kotlin.String? = null,

    @Schema(example = "This would have the java code for the tutorial", description = "")
    @get:JsonProperty("java") val java: kotlin.String? = null,

    @Schema(example = "This would have the python code for the tutorial", description = "")
    @get:JsonProperty("python") val python: kotlin.String? = null,

    @Schema(example = "null", description = "")
    @get:JsonProperty("image") val image: kotlin.String? = null
) {

}

