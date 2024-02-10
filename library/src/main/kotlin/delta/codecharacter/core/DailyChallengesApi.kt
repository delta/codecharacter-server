/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.1.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
*/
package delta.codecharacter.core

import delta.codecharacter.dtos.DailyChallengeGetRequestDto
import delta.codecharacter.dtos.DailyChallengeLeaderBoardResponseDto
import delta.codecharacter.dtos.DailyChallengeMatchRequestDto
import delta.codecharacter.dtos.GenericErrorDto
import delta.codecharacter.dtos.MatchDto
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.enums.*
import io.swagger.v3.oas.annotations.media.*
import io.swagger.v3.oas.annotations.responses.*
import io.swagger.v3.oas.annotations.security.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.*
import org.springframework.validation.annotation.Validated
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.beans.factory.annotation.Autowired

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jakarta.validation.Valid

import kotlin.collections.List
import kotlin.collections.Map

@Validated
@RequestMapping("\${api.base-path:}")
interface DailyChallengesApi {

    @Operation(
        summary = "Match Execution for Daily Challenges",
        operationId = "createDailyChallengeMatch",
        description = """Match making for Daily Challenges""",
        responses = [
            ApiResponse(responseCode = "201", description = "Created"),
            ApiResponse(responseCode = "400", description = "Bad Request", content = [Content(schema = Schema(implementation = GenericErrorDto::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ],
        security = [ SecurityRequirement(name = "http-bearer") ]
    )
    @RequestMapping(
            method = [RequestMethod.POST],
            value = ["/dc/submit"],
            produces = ["application/json"],
            consumes = ["application/json"]
    )
    fun createDailyChallengeMatch(@Parameter(description = "", required = true) @Valid @RequestBody dailyChallengeMatchRequestDto: DailyChallengeMatchRequestDto): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @Operation(
        summary = "Get Daily Challenge for the day",
        operationId = "getDailyChallenge",
        description = """Get current user challenge for that day""",
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [Content(schema = Schema(implementation = DailyChallengeGetRequestDto::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "404", description = "Not found")
        ],
        security = [ SecurityRequirement(name = "http-bearer") ]
    )
    @RequestMapping(
            method = [RequestMethod.GET],
            value = ["/dc/get"],
            produces = ["application/json"]
    )
    fun getDailyChallenge(): ResponseEntity<DailyChallengeGetRequestDto> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @Operation(
        summary = "Get Daily Challenges Leaderboard",
        operationId = "getDailyChallengeLeaderBoard",
        description = """Get Leaderboard for daily challenges""",
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [Content(array = ArraySchema(schema = Schema(implementation = DailyChallengeLeaderBoardResponseDto::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "404", description = "Not Found")
        ],
        security = [ SecurityRequirement(name = "http-bearer") ]
    )
    @RequestMapping(
            method = [RequestMethod.GET],
            value = ["/dc/leaderboard"],
            produces = ["application/json"]
    )
    fun getDailyChallengeLeaderBoard(@Parameter(description = "Index of the page") @Valid @RequestParam(value = "page", required = false) page: kotlin.Int?,@Parameter(description = "Size of the page") @Valid @RequestParam(value = "size", required = false) size: kotlin.Int?): ResponseEntity<List<DailyChallengeLeaderBoardResponseDto>> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @Operation(
        summary = "Get user daily challenge matches",
        operationId = "getUserDCMatches",
        description = """Get daily-challenge matches played by authenticated user""",
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [Content(array = ArraySchema(schema = Schema(implementation = MatchDto::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ],
        security = [ SecurityRequirement(name = "http-bearer") ]
    )
    @RequestMapping(
            method = [RequestMethod.GET],
            value = ["/dc/matches"],
            produces = ["application/json"]
    )
    fun getUserDCMatches(@Parameter(description = "Index of the page") @Valid @RequestParam(value = "page", required = false) page: kotlin.Int?,@Parameter(description = "Size of the page") @Valid @RequestParam(value = "size", required = false) size: kotlin.Int?): ResponseEntity<List<MatchDto>> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }
}
