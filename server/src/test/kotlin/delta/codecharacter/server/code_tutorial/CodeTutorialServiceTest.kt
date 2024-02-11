package delta.codecharacter.server.code_tutorial

import delta.codecharacter.dtos.ChallengeTypeDto
import delta.codecharacter.server.TestAttributes
import delta.codecharacter.server.code_tutorial.match.CodeTutorialMatchVerdictEnum
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.game.GameEntity
import delta.codecharacter.server.game.GameStatusEnum
import delta.codecharacter.server.notifications.NotificationService
import delta.codecharacter.server.user.public_user.PublicUserService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import java.util.UUID
import kotlin.collections.HashMap

internal class CodeTutorialServiceTest {
    private lateinit var codeTutorialRepository: CodeTutorialRepository
    private lateinit var publicUserService: PublicUserService
    private lateinit var codeTutorialService: CodeTutorialService
    private lateinit var notificationService: NotificationService

    @BeforeEach
    fun setUp() {
        codeTutorialRepository = mockk(relaxed = true)
        publicUserService = mockk(relaxed = true)
        notificationService = mockk(relaxed = true)
        codeTutorialService =
            CodeTutorialService(
                codeTutorialRepository, notificationService, publicUserService
            )

        ReflectionTestUtils.setField(codeTutorialService, "startDate", "2024-02-15T23:00:00Z")
        every { codeTutorialRepository.findByNumber(any()) } returns
            Optional.of(TestAttributes.codeTutorialCode)
    }

    @Test
    fun `should return code tutorial for User`() {
        assertThat(
            codeTutorialService.getTutorialByNumberForUser(UUID.randomUUID(), 0).tutorialType)
            .isEqualTo(ChallengeTypeDto.MAP)
    }
    @Test
    fun `should return failure if game had errors`() {
        val gameEntity =
                GameEntity(
                        id = UUID.randomUUID(),
                        coinsUsed = 2000,
                        destruction = 35.0,
                        status = GameStatusEnum.EXECUTE_ERROR,
                        matchId = UUID.randomUUID()
                )
        assertThat(codeTutorialService.completeCodeTutorial(gameEntity, UUID.randomUUID(), 1))
                .isEqualTo(CodeTutorialMatchVerdictEnum.FAILURE)
    }
    @Test
    fun `should return success if destruction met the required criteria`() {
        val gameEntity =
                GameEntity(
                        id = UUID.randomUUID(),
                        coinsUsed = 2000,
                        destruction = 90.0,
                        status = GameStatusEnum.EXECUTED,
                        matchId = UUID.randomUUID()
                )
        every { codeTutorialRepository.save(any()) } returns mockk()
        assertThat(codeTutorialService.completeCodeTutorial(gameEntity, UUID.randomUUID(), 2))
                .isEqualTo(CodeTutorialMatchVerdictEnum.SUCCESS)
    }
}
