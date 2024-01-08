package delta.codecharacter.server.code_tutorial

import com.fasterxml.jackson.databind.ObjectMapper
import delta.codecharacter.dtos.DailyChallengeMatchRequestDto
import delta.codecharacter.dtos.TutorialsGetRequestDto
import delta.codecharacter.server.TestAttributes
import delta.codecharacter.server.WithMockCustomUser
import delta.codecharacter.server.user.UserEntity
import delta.codecharacter.server.user.public_user.PublicUserEntity
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
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@AutoConfigureMockMvc
@SpringBootTest
internal class CodeTutorialControllerIntegrationTest(@Autowired val mockMvc: MockMvc) {

    @Autowired private lateinit var codeTutorialService: CodeTutorialService

    @Autowired private lateinit var jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder
    private lateinit var mapper: ObjectMapper

    @Autowired private lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun setUp() {
        mapper = jackson2ObjectMapperBuilder.build()
        mongoTemplate.save<UserEntity>(TestAttributes.user)
        mongoTemplate.save<PublicUserEntity>(TestAttributes.publicUser)
        ReflectionTestUtils.setField(codeTutorialService, "startDate", Instant.now().toString())
    }
    @Test
    @WithMockCustomUser
    fun `should not allow getting new tutorial if current one is not completed`() {
        val codeTutorialNumber = 3
        mockMvc
                .get("/codetutorial/get/$codeTutorialNumber") {
                    contentType = MediaType.APPLICATION_JSON
                }
                .andExpect {
                    status { isBadRequest() }
                    content {
                        mapper.writeValueAsString(
                                mapOf("message" to "Complete the current tutorial first")
                        )
                    }
                }
    }
    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection<UserEntity>()
        mongoTemplate.dropCollection<PublicUserEntity>()
    }
}
