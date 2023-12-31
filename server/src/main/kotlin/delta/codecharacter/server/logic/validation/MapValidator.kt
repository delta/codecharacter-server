package delta.codecharacter.server.logic.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import delta.codecharacter.server.config.GameConfiguration
import delta.codecharacter.server.exception.CustomException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus

class MapValidator {
    private val mapper = ObjectMapper().registerKotlinModule()
    private val gameParameters = GameConfiguration().gameParameters()
    private val invalidMapMessage = "Map is not valid"
    @Value("\${environment.max-limit-for-towers}") private var maxTowerLimit = 6000

    @Throws(CustomException::class)
    fun validateMap(mapJson: String) {
        val map: List<List<Int>>
        try {
            map = mapper.readValue(mapJson)
        } catch (_: Exception) {
            throw CustomException(HttpStatus.BAD_REQUEST, invalidMapMessage)
        }

        val validDefenderIds = gameParameters.defenders.map { it.id }
        val validDefenderPrice = gameParameters.defenders.map { it.price }
        val validDefenders =
            validDefenderIds.zip(validDefenderPrice).associateBy({ it.first }, { it.second })
        if (map.size != 64 || map.any { it.size != 64 }) {
            throw CustomException(HttpStatus.BAD_REQUEST, invalidMapMessage)
        }

        if (map.any { row -> row.any { cell -> cell !in validDefenderIds && cell != 0 } }) {
            throw CustomException(HttpStatus.BAD_REQUEST, invalidMapMessage)
        }
        var coinsUsedForMapTowers = 0
        map.forEach { row ->
            row.forEach { cell ->
                if (cell != 0 && validDefenders.containsKey(cell)) {
                    coinsUsedForMapTowers += validDefenders[cell] ?: 0
                }
            }
        }
        if (coinsUsedForMapTowers > maxTowerLimit) {
            throw CustomException(HttpStatus.BAD_REQUEST, invalidMapMessage)
        }

        if (map.withIndex().any { (i, row) ->
            row.withIndex().any { (j, cell) ->
                cell in validDefenderIds && (i == 0 || j == 0 || i == 63 || j == 63)
            }
        }
        ) {
            throw CustomException(HttpStatus.BAD_REQUEST, invalidMapMessage)
        }
    }
}
