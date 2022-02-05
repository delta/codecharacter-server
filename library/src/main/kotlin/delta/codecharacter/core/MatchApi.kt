/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (5.3.1).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package delta.codecharacter.core

import delta.codecharacter.dtos.CreateMatchRequestDto
import delta.codecharacter.dtos.GenericErrorDto
import delta.codecharacter.dtos.MatchDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.annotations.Authorization
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import javax.validation.Valid

@Validated
@Api(value = "Match", description = "The Match API")
@RequestMapping("\${api.base-path:}")
interface MatchApi {

    @ApiOperation(
        value = "Create match",
        nickname = "createMatch",
        notes = "Initiate a match by current user",
        authorizations = [Authorization(value = "http-bearer")]
    )
    @ApiResponses(
        value = [ApiResponse(
            code = 201,
            message = "Created"
        ), ApiResponse(
            code = 400,
            message = "Bad Request",
            response = GenericErrorDto::class
        ), ApiResponse(code = 401, message = "Unauthorized")]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/user/matches"],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    fun createMatch(
        @ApiParam(value = "") @Valid @RequestBody(required = false) createMatchRequestDto: CreateMatchRequestDto?
    ): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @ApiOperation(
        value = "Get top matches",
        nickname = "getTopMatches",
        notes = "Get top matches",
        response = MatchDto::class,
        responseContainer = "List",
        authorizations = [Authorization(value = "http-bearer")]
    )
    @ApiResponses(
        value = [ApiResponse(
            code = 200,
            message = "OK",
            response = MatchDto::class,
            responseContainer = "List"
        ), ApiResponse(code = 401, message = "Unauthorized")]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/top-matches"],
        produces = ["application/json"]
    )
    fun getTopMatches(): ResponseEntity<List<MatchDto>> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    @ApiOperation(
        value = "Get user matches",
        nickname = "getUserMatches",
        notes = "Get matches played by authenticated user",
        response = MatchDto::class,
        responseContainer = "List",
        authorizations = [Authorization(value = "http-bearer")]
    )
    @ApiResponses(
        value = [ApiResponse(
            code = 200,
            message = "OK",
            response = MatchDto::class,
            responseContainer = "List"
        ), ApiResponse(code = 401, message = "Unauthorized")]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/user/matches"],
        produces = ["application/json"]
    )
    fun getUserMatches(): ResponseEntity<List<MatchDto>> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }
}
