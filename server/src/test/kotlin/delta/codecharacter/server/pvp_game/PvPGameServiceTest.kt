package delta.codecharacter.server.pvp_game

import com.fasterxml.jackson.databind.ObjectMapper
import delta.codecharacter.server.code.LanguageEnum
import delta.codecharacter.server.config.GameConfiguration
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.params.GameCode
import delta.codecharacter.server.params.GameParameters
import delta.codecharacter.server.pvp_game.pvp_game_log.PvPGameLogService
import delta.codecharacter.server.pvp_game.queue.entities.PvPGameRequestEntity
import delta.codecharacter.server.pvp_game.queue.entities.PvPGameResultEntity
import delta.codecharacter.server.pvp_game.queue.entities.PvPGameStatusUpdateEntity
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.util.UUID
import java.util.Optional
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.http.HttpStatus

internal class PvPGameServiceTest {
    private lateinit var pvPGameRepository: PvPGameRepository
    private lateinit var pvPGameService: PvPGameService
    private lateinit var pvPGameLogService: PvPGameLogService
    private lateinit var rabbitTemplate: RabbitTemplate
    private lateinit var mapper: ObjectMapper
    private lateinit var gameParameters: GameParameters


    @BeforeEach
    fun setUp() {
        pvPGameRepository = mockk(relaxed = true)
        pvPGameLogService = mockk(relaxed = true)
        rabbitTemplate = mockk(relaxed = true)
        mapper = ObjectMapper()
        val gameConfiguration = GameConfiguration()
        gameParameters = gameConfiguration.gameParameters()

        pvPGameService = PvPGameService(pvPGameRepository, pvPGameLogService, rabbitTemplate, gameParameters)
    }

    @Test
    fun `should return pvp game by id`() {
        val pvPGameEntity = mockk<PvPGameEntity>()
        val gameId = UUID.randomUUID() // gameId and matchId are the same for PvP Game

        every { pvPGameRepository.findById(any()) } returns Optional.of(pvPGameEntity)

        val result = pvPGameService.getPvPGame(gameId)
        assertEquals(pvPGameEntity, result)

        verify { pvPGameRepository.findById(gameId) }
        confirmVerified(pvPGameRepository)
    }

    @Test
    @Throws(CustomException::class)
    fun `should throw exception if pvp game not found`() {
        val gameId = UUID.randomUUID()

        every { pvPGameRepository.findById(any()) } returns Optional.empty()

        val exception = assertThrows(CustomException::class.java) {
            pvPGameService.getPvPGame(gameId)
        }

        assertEquals(exception.status, HttpStatus.NOT_FOUND)

        verify { pvPGameRepository.findById(gameId) }
        confirmVerified(pvPGameRepository)
    }

    @Test
    fun `should create pvp game request`() {
        val pvPGame = mockk<PvPGameEntity>()
        val matchId = UUID.randomUUID()

        val expectedPvPGameRequest =
            PvPGameRequestEntity(
                gameId = matchId,
                player1 = GameCode("player1 code", LanguageEnum.CPP),
                player2 = GameCode("player2 code", LanguageEnum.JAVA),
                parameters = gameParameters,
            )

        every {pvPGame.matchId} returns matchId
        every {
            rabbitTemplate.convertAndSend(
                "gamePvPRequestQueue",
                mapper.writeValueAsString(expectedPvPGameRequest)
            )
        } returns Unit

        pvPGameService.sendPvPGameRequest(pvPGame, expectedPvPGameRequest.player1, expectedPvPGameRequest.player2)

        verify {
            rabbitTemplate.convertAndSend(
                "gamePvPRequestQueue",
                mapper.writeValueAsString(expectedPvPGameRequest)
            )
        }
        confirmVerified(rabbitTemplate)
    }

    @Test
    fun `should receive pvp game status update`() {
        val pvPGame =
            PvPGameEntity(
                matchId = UUID.randomUUID(),
                scorePlayer1 = 0,
                scorePlayer2 = 0,
                status = PvPGameStatusEnum.IDLE,
            )

        val pvPGameStatusUpdate =
            PvPGameStatusUpdateEntity(
                gameId = pvPGame.matchId,
                gameStatus = PvPGameStatusEnum.EXECUTING,
                gameResultPlayer1 = null,
                gameResultPlayer2 = null
            )


        every { pvPGameRepository.findById(pvPGame.matchId) } returns Optional.of(pvPGame)
        every {
            pvPGameRepository.save(
                PvPGameEntity(
                    pvPGame.matchId,
                    pvPGame.scorePlayer1,
                    pvPGame.scorePlayer2,
                    PvPGameStatusEnum.EXECUTING,
                )
            )
        } returns mockk()

        pvPGameService.updateGameStatus(mapper.writeValueAsString(pvPGameStatusUpdate))

        verify { pvPGameRepository.findById(pvPGame.matchId) }
        verify {
            pvPGameRepository.save(
                PvPGameEntity(
                    pvPGame.matchId,
                    pvPGame.scorePlayer1,
                    pvPGame.scorePlayer2,
                    PvPGameStatusEnum.EXECUTING,
                )
            )
        }
        confirmVerified(pvPGameRepository)
    }

    @Test
    fun `should receive pvp game status update with result`() {
        val pvPGame =
            PvPGameEntity(
                matchId = UUID.randomUUID(),
                scorePlayer1 = 0,
                scorePlayer2 = 0,
                status = PvPGameStatusEnum.IDLE,
            )
        val pvPGameStatusUpdate =
            PvPGameStatusUpdateEntity(
                gameId = pvPGame.matchId,
                gameStatus = PvPGameStatusEnum.EXECUTED,
                gameResultPlayer1 =
                    PvPGameResultEntity(
                        score = 100,
                        hasErrors = false,
                        log = "player1 log"
                    ),
                gameResultPlayer2 =
                    PvPGameResultEntity(
                        score = 100,
                        hasErrors = false,
                        log = "player2 log"
                    ),
            )

        val updatedPvPGameEntity =
            PvPGameEntity(
                matchId = pvPGame.matchId,
                scorePlayer1 = pvPGameStatusUpdate.gameResultPlayer1!!.score,
                scorePlayer2 = pvPGameStatusUpdate.gameResultPlayer2!!.score,
                status = pvPGameStatusUpdate.gameStatus,
            )

        every { pvPGameRepository.findById(pvPGame.matchId) } returns Optional.of(pvPGame)
        every { pvPGameRepository.save(updatedPvPGameEntity) } returns updatedPvPGameEntity

        pvPGameService.updateGameStatus(mapper.writeValueAsString(pvPGameStatusUpdate))

        verify { pvPGameRepository.findById(pvPGame.matchId) }
        verify {
            pvPGameRepository.save(
                PvPGameEntity (
                    matchId = pvPGame.matchId,
                    scorePlayer1 = pvPGameStatusUpdate.gameResultPlayer1!!.score,
                    scorePlayer2 = pvPGameStatusUpdate.gameResultPlayer2!!.score,
                    status = pvPGameStatusUpdate.gameStatus,
                )
            )
        }
        confirmVerified(pvPGameRepository)
    }
}
