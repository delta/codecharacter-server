package delta.codecharacter.dtos

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonValue
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
* 
* Values: NEXT,PREVIOUS,SKIP,RESET
*/
enum class TutorialUpdateTypeDto(val value: kotlin.String) {

    @JsonProperty("NEXT") NEXT("NEXT"),
    @JsonProperty("PREVIOUS") PREVIOUS("PREVIOUS"),
    @JsonProperty("SKIP") SKIP("SKIP"),
    @JsonProperty("RESET") RESET("RESET")
}

