package delta.codecharacter.server.pvp_game

import com.fasterxml.jackson.databind.ObjectMapper
import delta.codecharacter.server.TestAttributes
import delta.codecharacter.server.WithMockCustomUser
import delta.codecharacter.server.match.MatchModeEnum
import delta.codecharacter.server.match.MatchVerdictEnum
import delta.codecharacter.server.match.PvPMatchEntity
import delta.codecharacter.server.pvp_game.pvp_game_log.PvPGameLogEntity
import delta.codecharacter.server.user.UserEntity
import delta.codecharacter.server.user.public_user.PublicUserEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant
import java.util.UUID

@AutoConfigureMockMvc
@SpringBootTest
internal class PvPGameControllerIntegrationTest(@Autowired val mockMvc: MockMvc) {
    @Autowired private lateinit var mongoTemplate: MongoTemplate

    @Autowired private lateinit var jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder
    private lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mapper = jackson2ObjectMapperBuilder.build()
        mongoTemplate.save<UserEntity>(TestAttributes.user)
        mongoTemplate.save<UserEntity>(TestAttributes.opponent)
        mongoTemplate.save<PublicUserEntity>(TestAttributes.publicUser)
        mongoTemplate.save<PublicUserEntity>(TestAttributes.publicOpponent)

        val context = SecurityContextHolder.createEmptyContext()
        val auth: Authentication =
            UsernamePasswordAuthenticationToken(
                TestAttributes.user, TestAttributes.user.password, TestAttributes.user.authorities
            )
        context.authentication = auth
        SecurityContextHolder.setContext(context)
    }

    @Test
    @WithMockCustomUser
    fun `should get pvp game log`() {
        val pvPGameEntity =
            PvPGameEntity(
                matchId = UUID.randomUUID(),
                destructionPlayer1 = 100.0,
                destructionPlayer2 = 100.0,
                coinsUsedPlayer1 = 100,
                coinsUsedPlayer2 = 100,
                status = PvPGameStatusEnum.EXECUTED,
            )
        mongoTemplate.save<PvPGameEntity>(pvPGameEntity)

        val pvPMatchEntity =
            PvPMatchEntity(
                id = pvPGameEntity.matchId,
                player1 = TestAttributes.publicUser,
                player2 = TestAttributes.publicOpponent,
                game = pvPGameEntity,
                mode = MatchModeEnum.PVP,
                verdict = MatchVerdictEnum.PLAYER1,
                createdAt = Instant.now(),
                totalPoints = 100,
            )
        mongoTemplate.save<PvPMatchEntity>(pvPMatchEntity)

        val pvPGameLogEntity = PvPGameLogEntity(gameId = pvPGameEntity.matchId, player1Log = "game log player 1", player2Log = "game log player 2")
        mongoTemplate.save<PvPGameLogEntity>(pvPGameLogEntity)

        mockMvc
            .get("/pvpgames/${pvPGameEntity.matchId}/logs") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { string(pvPGameLogEntity.player1Log) }
            }
    }

    @Test
    @WithMockCustomUser
    fun `should return empty string when pvp game or match not found`() {
        val randomUUID = UUID.randomUUID()

        mockMvc.get("/pvpgames/${randomUUID}/logs") { contentType = MediaType.APPLICATION_JSON }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("") }
        }
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection<UserEntity>()
        mongoTemplate.dropCollection<PublicUserEntity>()
        mongoTemplate.dropCollection<PvPGameEntity>()
        mongoTemplate.dropCollection<PvPMatchEntity>()
        mongoTemplate.dropCollection<PvPGameLogEntity>()

        SecurityContextHolder.clearContext()
    }
}
