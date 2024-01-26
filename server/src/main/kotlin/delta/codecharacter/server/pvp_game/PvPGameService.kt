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
        val pvPGameRequest =
            PvPGameRequestEntity(
                gameId = pvPGame.matchId,
                parameters = parameters,
                player1 = player1Code,
                player2 = player2Code
            )
        rabbitTemplate.convertAndSend("gamePvPRequestQueue", mapper.writeValueAsString(pvPGameRequest))
    }

    fun updateGameStatus(gameStatusUpdateJson: String): PvPGameEntity {
        val gameStatusUpdateEntity =
            mapper.readValue(gameStatusUpdateJson, PvPGameStatusUpdateEntity::class.java)
        val oldPvPGameEntity =
            pvPGameRepository.findById(gameStatusUpdateEntity.gameId).orElseThrow {
                throw CustomException(HttpStatus.NOT_FOUND, "PvPGame not found")
            }
        if(gameStatusUpdateEntity.gameResultPlayer1 == null || gameStatusUpdateEntity.gameResultPlayer2 == null) {
            val newPvPGameEntity = oldPvPGameEntity.copy(status = gameStatusUpdateEntity.gameStatus)
            println("newPvPGameEntity: $newPvPGameEntity")
            println("type of newPvPGameEntity: ${newPvPGameEntity::class.java}")
            println("pvPGame: ${pvPGameRepository.save(newPvPGameEntity)}")
            println("type of pvPGame: ${pvPGameRepository.save(newPvPGameEntity)::class.java}")
            //val pvPGame = pvPGameRepository.save(newPvPGameEntity)
            return oldPvPGameEntity
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
        pvPGameLogService.savePvPGameLog(pvPGame.matchId, gameResultPlayer1.log, gameResultPlayer2.log)
        return pvPGame
    }
}
