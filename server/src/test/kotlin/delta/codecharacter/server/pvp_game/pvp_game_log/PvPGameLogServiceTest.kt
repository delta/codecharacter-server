package delta.codecharacter.server.pvp_game.pvp_game_log

import delta.codecharacter.server.TestAttributes
import delta.codecharacter.server.match.PvPMatchEntity
import delta.codecharacter.server.match.PvPMatchRepository
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

internal class PvPGameLogServiceTest {
    private lateinit var pvPGameLogRepository: PvPGameLogRepository
    private lateinit var pvPMatchRepository: PvPMatchRepository
    private lateinit var pvPGameLogService: PvPGameLogService
    private lateinit var pvPMatchEntity: PvPMatchEntity

    @BeforeEach
    fun setUp() {
        pvPGameLogRepository = mockk()
        pvPMatchRepository = mockk()
        pvPMatchEntity = mockk()

        pvPGameLogService = PvPGameLogService(pvPGameLogRepository, pvPMatchRepository)
    }

    @Test
    fun `should return pvp game log`() {
        val gameId = UUID.randomUUID()
        val player1Id = UUID.randomUUID()
        val player2Id = UUID.randomUUID()

        val player1 = TestAttributes.publicUser.copy(userId = player1Id, username = "player1")
        val player2 = TestAttributes.publicUser.copy(userId = player2Id, username = "player2")

        val pvpMatchEntity = mockk<PvPMatchEntity>()
        every { pvpMatchEntity.player1 } returns player1
        every { pvpMatchEntity.player2 } returns player2

        val pvPGameLogEntity = mockk<PvPGameLogEntity>()
        val expectedPvPGameLogPlayer1 = "pvp game log player 1"
        val expectedPvPGameLogPlayer2 = "pvp game log player 2"

        every { pvPGameLogRepository.findById(gameId) } returns Optional.of(pvPGameLogEntity)
        every { pvPMatchRepository.findById(gameId) } returns Optional.of(pvpMatchEntity)
        every { pvPGameLogEntity.player1Log } returns expectedPvPGameLogPlayer1
        every { pvPGameLogEntity.player2Log } returns expectedPvPGameLogPlayer2


        val pvPGameLogPlayer1 = pvPGameLogService.getPlayerLog(gameId, player1Id)
        val pvPGameLogPlayer2 = pvPGameLogService.getPlayerLog(gameId, player2Id)

        assertEquals(expectedPvPGameLogPlayer1, pvPGameLogPlayer1)
        assertEquals(expectedPvPGameLogPlayer2, pvPGameLogPlayer2)

        verify { pvpMatchEntity.player1 }
        verify { pvpMatchEntity.player2 }

        verify { pvPGameLogRepository.findById(gameId) }
        verify { pvPMatchRepository.findById(gameId) }
        verify { pvPGameLogEntity.player1Log }
        verify { pvPGameLogEntity.player2Log }

        confirmVerified(pvPGameLogRepository)
    }

}
