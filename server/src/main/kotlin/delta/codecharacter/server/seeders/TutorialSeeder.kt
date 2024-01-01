package delta.codecharacter.server.seeders

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import delta.codecharacter.server.tutorial.TutorialEntity
import delta.codecharacter.server.tutorial.TutorialRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TutorialSeeder {

    @Autowired private lateinit var tutorialRepository: TutorialRepository

    private val logger: Logger = LoggerFactory.getLogger(TutorialSeeder::class.java)
    @EventListener(ApplicationReadyEvent::class)
    fun seedTutorials() {

        if (tutorialRepository.findAll().isEmpty()) {
            logger.info("Seeding tutorials")

            val jsonString = this::class.java.classLoader.getResource("tutorialConstants.json")?.readText()
            if (!jsonString.isNullOrEmpty()) {
                val objectMapper = jacksonObjectMapper()
                val tuts: List<TutorialObject> = objectMapper.readValue(jsonString)
                var tutEntities: List<TutorialEntity> = listOf()
                tuts.forEach {
                    val id = UUID.randomUUID()
                    tutEntities =
                            tutEntities.plus(
                                    TutorialEntity(
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
                tutorialRepository.saveAll(tutEntities)
                logger.info("Seeding Tutorials Completed")
            } else {
                logger.error("tutorialConstants.json is empty or doesn't exist")
            }
        } else {
            logger.info("Tutorials seeded already")
        }
    }
}
