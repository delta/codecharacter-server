package delta.codecharacter.server.seeders

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import delta.codecharacter.server.code_tutorial.CodeTutorialEntity
import delta.codecharacter.server.code_tutorial.CodeTutorialRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CodeTutorialSeeder {

    @Autowired private lateinit var codeTutorialRepository: CodeTutorialRepository

    private val logger: Logger = LoggerFactory.getLogger(CodeTutorialSeeder::class.java)
    @EventListener(ApplicationReadyEvent::class)
    fun seedTutorials() {

        if (codeTutorialRepository.findAll().isEmpty()) {
            logger.info("Seeding tutorials")

            val jsonString = this::class.java.classLoader.getResource("tutorialConstants.json")?.readText()
            if (!jsonString.isNullOrEmpty()) {
                val objectMapper = jacksonObjectMapper()
                val tuts: List<CodeTutorialObject> = objectMapper.readValue(jsonString)
                var tutEntities: List<CodeTutorialEntity> = listOf()
                tuts.forEach {
                    val id = UUID.randomUUID()
                    tutEntities =
                            tutEntities.plus(
                                    CodeTutorialEntity(
                                            id = id,
                                            number = it.number,
                                            tutName = it.tutName,
                                            tutType = it.tutType,
                                            tutorial = it.tutorial,
                                            description = it.description,
                                            map = it.map,
                                    )
                            )
                }
                codeTutorialRepository.saveAll(tutEntities)
                logger.info("Seeding Tutorials Completed")
            } else {
                logger.error("tutorialConstants.json is empty or doesn't exist")
            }
        } else {
            logger.info("Tutorials seeded already")
        }
    }
}
