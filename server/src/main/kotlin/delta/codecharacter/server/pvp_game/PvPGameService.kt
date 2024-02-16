package delta.codecharacter.server.pvp_game

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.params.GameCode
import delta.codecharacter.server.params.GameParameters
import delta.codecharacter.server.pvp_game.pvp_game_log.PvPGameLogService
import delta.codecharacter.server.pvp_game.queue.entities.PvPGameRequestEntity
import delta.codecharacter.server.pvp_game.queue.entities.PvPGameStatusUpdateEntity
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PvPGameService(
    @Autowired private val pvPGameRepository: PvPGameRepository,
    @Autowired private val pvPGameLogService: PvPGameLogService,
    @Autowired private val rabbitTemplate: RabbitTemplate,
    @Autowired private val parameters: GameParameters
) {
    private var mapper = ObjectMapper().registerKotlinModule()

    fun getPvPGame(matchId: UUID): PvPGameEntity {
        return pvPGameRepository.findById(matchId).orElseThrow {
            throw CustomException(HttpStatus.NOT_FOUND, "PvPGame not found")
        }
    }

    fun createPvPGame(matchId: UUID): PvPGameEntity {
        val pvPGame =
            PvPGameEntity(
                matchId = matchId,
                scorePlayer1 = 0,
                scorePlayer2 = 0,
                status = PvPGameStatusEnum.IDLE,
            )
        return pvPGameRepository.save(pvPGame)
    }

    fun sendPvPGameRequest(pvPGame: PvPGameEntity, player1Code: GameCode, player2Code: GameCode) {
        val pvPGameParameters = parameters.copy(
            numberOfCoins = 10,
        )
        val pvPGameRequest =
            PvPGameRequestEntity(
                gameId = pvPGame.matchId,
                parameters = pvPGameParameters,
                player1 = player1Code,
                player2 = player2Code
            )
        rabbitTemplate.convertAndSend("gamePvpRequestQueue", mapper.writeValueAsString(pvPGameRequest))
    }

    fun updateGameStatus(gameStatusUpdateJson: String): Triple<PvPGameEntity, Boolean, Boolean> {
        val gameStatusUpdateEntity =
            mapper.readValue(gameStatusUpdateJson, PvPGameStatusUpdateEntity::class.java)
        val oldPvPGameEntity =
            pvPGameRepository.findById(gameStatusUpdateEntity.gameId).orElseThrow {
                throw CustomException(HttpStatus.NOT_FOUND, "PvPGame not found")
            }
        if(gameStatusUpdateEntity.gameResultPlayer1 == null || gameStatusUpdateEntity.gameResultPlayer2 == null) {
            val newPvPGameEntity = oldPvPGameEntity.copy(status = gameStatusUpdateEntity.gameStatus)
            return Triple(pvPGameRepository.save(newPvPGameEntity), false, false)
        }

        val gameResultPlayer1 = gameStatusUpdateEntity.gameResultPlayer1
        val gameResultPlayer2 = gameStatusUpdateEntity.gameResultPlayer2

        val gameStatus =
            if (gameResultPlayer1.hasErrors || gameResultPlayer2.hasErrors) {
                PvPGameStatusEnum.EXECUTE_ERROR
            } else {
                PvPGameStatusEnum.EXECUTED
            }

        val newPvPGameEntity =
            oldPvPGameEntity.copy(
                scorePlayer1 = gameResultPlayer1.score,
                scorePlayer2 = gameResultPlayer2.score,
                status = gameStatus
            )
        val pvPGame = pvPGameRepository.save(newPvPGameEntity)
        println(gameResultPlayer1.log.length)
        println(gameResultPlayer2.log.length)
        try {
            pvPGameLogService.savePvPGameLog(pvPGame.matchId, gameResultPlayer1.log, gameResultPlayer2.log)
        }
        catch (e : Exception){
            println("log size too much")
            val logSizeHighError = "ERRORS, ERROR TYPE: LOGS TOO BIG\nERRORS, ERROR LOG:\n THE GAME RAN PERFECTLY BUT THE LOGS ARE TOO BIG TO RENDER\n"
            pvPGameLogService.savePvPGameLog(pvPGame.matchId,logSizeHighError,logSizeHighError)
        }
        return Triple(pvPGame, gameResultPlayer1.hasErrors, gameResultPlayer2.hasErrors)
    }
}
