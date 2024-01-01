package delta.codecharacter.server.seeders

import com.fasterxml.jackson.annotation.JsonProperty
import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.dtos.DailyChallengeObjectDto
import delta.codecharacter.dtos.TutorialCodeObjectDto

data class TutorialObject(
        @field:JsonProperty("number") val number: Int,
        @field:JsonProperty("tutName") val tutName: String,
        @field:JsonProperty("tutType") val tutType: ChallengeTypeDto,
        @field:JsonProperty("tutorial") val tutorial: TutorialCodeObjectDto,
        @field:JsonProperty("description") val description: String?,
        @field:JsonProperty("map") val map: String
)
