package delta.codecharacter.server.match

import com.fasterxml.jackson.databind.ObjectMapper
import delta.codecharacter.dtos.*
import delta.codecharacter.server.code.Code
import delta.codecharacter.server.code.LanguageEnum
import delta.codecharacter.server.code.code_revision.CodeRevisionService
import delta.codecharacter.server.code.latest_code.LatestCodeService
import delta.codecharacter.server.code.locked_code.LockedCodeService
import delta.codecharacter.server.daily_challenge.DailyChallengeService
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchEntity
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchRepository
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.exception.CustomException
import delta.codecharacter.server.game.GameRepository
import delta.codecharacter.server.game.GameService
import delta.codecharacter.server.game.GameStatusEnum
import delta.codecharacter.server.game.queue.entities.GameStatusUpdateEntity
import delta.codecharacter.server.game_map.latest_map.LatestMapService
import delta.codecharacter.server.game_map.locked_map.LockedMapService
import delta.codecharacter.server.game_map.map_revision.MapRevisionService
import delta.codecharacter.server.logic.validation.MapValidator
import delta.codecharacter.server.logic.verdict.VerdictAlgorithm
import delta.codecharacter.server.notifications.NotificationService
import delta.codecharacter.server.params.GameCode
import delta.codecharacter.server.pvp_game.PvPGameRepository
import delta.codecharacter.server.pvp_game.PvPGameService
import delta.codecharacter.server.pvp_game.PvPGameStatusEnum
import delta.codecharacter.server.user.public_user.PublicUserEntity
import delta.codecharacter.server.user.public_user.PublicUserService
import delta.codecharacter.server.user.rating_history.RatingHistoryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class MatchService(
    @Autowired private val matchRepository: MatchRepository,
    @Autowired private val gameService: GameService,
    @Autowired private val pvPGameService: PvPGameService,
    @Autowired private val latestCodeService: LatestCodeService,
    @Autowired private val codeRevisionService: CodeRevisionService,
    @Autowired private val lockedCodeService: LockedCodeService,
    @Autowired private val latestMapService: LatestMapService,
    @Autowired private val mapRevisionService: MapRevisionService,
    @Autowired private val lockedMapService: LockedMapService,
    @Autowired private val publicUserService: PublicUserService,
    @Autowired private val verdictAlgorithm: VerdictAlgorithm,
    @Autowired private val ratingHistoryService: RatingHistoryService,
    @Autowired private val notificationService: NotificationService,
    @Autowired private val dailyChallengeService: DailyChallengeService,
    @Autowired private val dailyChallengeMatchRepository: DailyChallengeMatchRepository,
    @Autowired private val jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder,
    @Autowired private val simpMessagingTemplate: SimpMessagingTemplate,
    @Autowired private val mapValidator: MapValidator,
    @Autowired private val autoMatchRepository: AutoMatchRepository,
    @Autowired private val pvPMatchRepository: PvPMatchRepository,
    @Autowired private val gameRepository: GameRepository,
    @Autowired private val pvPGameRepository: PvPGameRepository,
) {
    private var mapper: ObjectMapper = jackson2ObjectMapperBuilder.build()
    private val logger: Logger = LoggerFactory.getLogger(MatchService::class.java)

    private fun getCodeFromRevision(userId: UUID, codeRevisionId: UUID?, codeType: CodeTypeDto): Pair<String, LanguageEnum> {
        when (codeRevisionId) {
            null -> {
                val latestCode = latestCodeService.getLatestCode(userId, codeType)
                return Pair(latestCode.code, LanguageEnum.valueOf(latestCode.language.name))
            }
            else -> {
                val codeRevision =
                    codeRevisionService.getCodeRevisions(userId, codeType).find { it.id == codeRevisionId}
                        ?: throw CustomException(HttpStatus.BAD_REQUEST, "Invalid revision ID")
                return Pair(codeRevision.code, LanguageEnum.valueOf(codeRevision.language.name))
            }
        }
    }

    private fun createNormalSelfMatch(userId: UUID, codeRevisionId: UUID?, mapRevisionId: UUID?) {
        val (code, language) = getCodeFromRevision(userId, codeRevisionId, CodeTypeDto.NORMAL)
        val map: String =
            if (mapRevisionId == null) {
                val latestMap = latestMapService.getLatestMap(userId)
                latestMap.map
            } else {
                val mapRevision =
                    mapRevisionService.getMapRevisions(userId).find { it.id == mapRevisionId }
                        ?: throw CustomException(HttpStatus.BAD_REQUEST, "Invalid revision ID")
                mapRevision.map
            }

        val matchId = UUID.randomUUID()
        val game = gameService.createGame(matchId)
        val publicUser = publicUserService.getPublicUser(userId)
        val match =
            MatchEntity(
                id = matchId,
                games = listOf(game),
                mode = MatchModeEnum.SELF,
                verdict = MatchVerdictEnum.TIE,
                createdAt = Instant.now(),
                totalPoints = 0,
                player1 = publicUser,
                player2 = publicUser,
            )
        matchRepository.save(match)
        gameService.sendGameRequest(game, code, LanguageEnum.valueOf(language.name), map)
    }

    private fun createPvPSelfMatch(userId: UUID, codeRevisionId1: UUID?, codeRevisionId2: UUID?) {
        if (codeRevisionId1==codeRevisionId2) {
            throw CustomException(HttpStatus.BAD_REQUEST, "Codes must be different")
        }
        println(codeRevisionId1)
        println(codeRevisionId2)
        val (code1, language1) = getCodeFromRevision(userId, codeRevisionId1, CodeTypeDto.PVP)
        val (code2, language2) = getCodeFromRevision(userId, codeRevisionId2, CodeTypeDto.PVP)
        val matchId = UUID.randomUUID()
        val game = pvPGameService.createPvPGame(matchId)
        val publicUser = publicUserService.getPublicUser(userId)
        val match =
            PvPMatchEntity(
                id = matchId,
                game = game,
                mode = MatchModeEnum.PVP,
                verdict = MatchVerdictEnum.TIE,
                createdAt = Instant.now(),
                totalPoints = 0,
                player1 = publicUser,
                player2 = publicUser,
            )
        pvPMatchRepository.save(match)
        pvPGameService.sendPvPGameRequest(game, GameCode(code1, language1), GameCode(code2, language2))
    }

    private fun createNormalMatch(
        publicUser: PublicUserEntity,
        publicOpponent: PublicUserEntity,
        mode: MatchModeEnum
    ) : UUID {
        val userId = publicUser.userId
        val opponentId = publicOpponent.userId
        val (userLanguage, userCode) = lockedCodeService.getLockedCode(userId, CodeTypeDto.NORMAL)
        val userMap = lockedMapService.getLockedMap(userId)

        val (opponentLanguage, opponentCode) = lockedCodeService.getLockedCode(opponentId, CodeTypeDto.NORMAL)
        val opponentMap = lockedMapService.getLockedMap(opponentId)

        val matchId = UUID.randomUUID()

        val game1 = gameService.createGame(matchId)
        val game2 = gameService.createGame(matchId)

        val match =
            MatchEntity(
                id = matchId,
                games = listOf(game1, game2),
                mode = mode,
                verdict = MatchVerdictEnum.TIE,
                createdAt = Instant.now(),
                totalPoints = 0,
                player1 = publicUser,
                player2 = publicOpponent,
            )
        matchRepository.save(match)

        gameService.sendGameRequest(game1, userCode, userLanguage, opponentMap)
        gameService.sendGameRequest(game2, opponentCode, opponentLanguage, userMap)
        if (mode == MatchModeEnum.AUTO) {
            logger.info(
                "Auto match started between ${match.player1.username} and ${match.player2.username}"
            )
        }
        return matchId
    }

    private fun createPvPMatch(publicUser: PublicUserEntity, publicOpponent: PublicUserEntity) : UUID {
        val userId = publicUser.userId
        val opponentId = publicOpponent.userId

        val (userLanguage, userCode) = lockedCodeService.getLockedCode(userId, CodeTypeDto.PVP)
        val (opponentLanguage, opponentCode) = lockedCodeService.getLockedCode(opponentId, CodeTypeDto.PVP)

        val matchId = UUID.randomUUID()

        val game = pvPGameService.createPvPGame(matchId)

        val match =
            PvPMatchEntity(
                id = matchId,
                game = game,
                mode = MatchModeEnum.PVP,
                verdict = MatchVerdictEnum.TIE,
                createdAt = Instant.now(),
                totalPoints = 0,
                player1 = publicUser,
                player2 = publicOpponent,
            )
        pvPMatchRepository.save(match)

        pvPGameService.sendPvPGameRequest(game, GameCode(userCode, userLanguage), GameCode(opponentCode, opponentLanguage))

        return matchId
    }

    private fun createDualMatch(userId: UUID, opponentUsername: String, mode: MatchModeEnum): UUID {
        val publicUser = publicUserService.getPublicUser(userId)
        val publicOpponent = publicUserService.getPublicUserByUsername(opponentUsername)
        val opponentId = publicOpponent.userId
        if (userId == opponentId) {
            throw CustomException(HttpStatus.BAD_REQUEST, "You cannot play against yourself")
        }
        return when(mode) {
            MatchModeEnum.MANUAL, MatchModeEnum.AUTO -> {
                createNormalMatch(publicUser, publicOpponent, mode)
            }
            MatchModeEnum.PVP -> {
                createPvPMatch(publicUser, publicOpponent)
            }
            else -> {
                throw CustomException(HttpStatus.BAD_REQUEST, "MatchMode does not exist")
            }
        }
    }

    fun createDCMatch(userId: UUID, dailyChallengeMatchRequestDto: DailyChallengeMatchRequestDto) {
        val (_, chall, challType, _, completionStatus) =
            dailyChallengeService.getDailyChallengeByDateForUser(userId)
        if (completionStatus != null && completionStatus) {
            throw CustomException(
                HttpStatus.BAD_REQUEST, "You have already completed your daily challenge"
            )
        }
        val dc = dailyChallengeService.getDailyChallengeByDate()
        val (value, _) = dailyChallengeMatchRequestDto
        val language: LanguageEnum
        val map: String
        val code: String
        when (challType) {
            ChallengeTypeDto.CODE -> { // code as question and map as answer
                mapValidator.validateMap(value)
                code = chall.cpp.toString()
                language = LanguageEnum.CPP
                map = value
            }
            ChallengeTypeDto.MAP -> {
                map = dc.map
                language = LanguageEnum.valueOf(dailyChallengeMatchRequestDto.language.toString())
                code = value
            }
        }
        val matchId = UUID.randomUUID()
        val game = gameService.createGame(matchId)
        val user = publicUserService.getPublicUser(userId)
        val match =
            DailyChallengeMatchEntity(
                id = matchId,
                verdict = DailyChallengeMatchVerdictEnum.STARTED,
                createdAt = Instant.now(),
                user = user,
                game = game
            )
        dailyChallengeMatchRepository.save(match)
        gameService.sendGameRequest(game, code, language, map)
    }
    fun createMatch(userId: UUID, createMatchRequestDto: CreateMatchRequestDto) {
        println(createMatchRequestDto)
        when (createMatchRequestDto.mode) {
            MatchModeDto.SELF -> {
                val (_, _, mapRevisionId, codeRevisionId, _) = createMatchRequestDto
                createNormalSelfMatch(userId, codeRevisionId, mapRevisionId)
            }
            MatchModeDto.SELFPVP -> {
                val (_, _, _, codeRevisionId1, codeRevisionId2) = createMatchRequestDto
                createPvPSelfMatch(userId, codeRevisionId1, codeRevisionId2)
            }
            MatchModeDto.MANUAL, MatchModeDto.AUTO , MatchModeDto.PVP -> {
                if (createMatchRequestDto.opponentUsername == null) {
                    throw CustomException(HttpStatus.BAD_REQUEST, "Opponent ID is required")
                }
                createDualMatch(userId, createMatchRequestDto.opponentUsername!!, MatchModeEnum.valueOf(createMatchRequestDto.mode.name))
            }
            else -> {
                throw CustomException(HttpStatus.BAD_REQUEST, "MatchMode Is Not Correct")
            }
        }
    }

    fun createAutoMatch() {
        val topNUsers = publicUserService.getTopNUsers()
        val userIds = topNUsers.map { it.userId }
        val usernames = topNUsers.map { it.username }
        logger.info("Auto matches started for users: $usernames")
        autoMatchRepository.deleteAll()
        userIds.forEachIndexed { i, userId ->
            run {
                for (j in i + 1 until userIds.size) {
                    val opponentUsername = usernames[j]
                    val matchId = createDualMatch(userId, opponentUsername, MatchModeEnum.AUTO)
                    autoMatchRepository.save(AutoMatchEntity(matchId, 0))
                }
            }
        }
    }

    private fun mapMatchEntitiesToDtos(matchEntities: List<MatchEntity>): List<MatchDto> {
        return matchEntities.map { matchEntity ->
            MatchDto(
                id = matchEntity.id,
                matchMode = MatchModeDto.valueOf(matchEntity.mode.name),
                matchVerdict = VerdictDto.valueOf(matchEntity.verdict.name),
                createdAt = matchEntity.createdAt,
                games =
                matchEntity
                    .games
                    .map { gameEntity ->
                        GameDto(
                            id = gameEntity.id,
                            destruction = BigDecimal(gameEntity.destruction),
                            coinsUsed = gameEntity.coinsUsed,
                            status = GameStatusDto.valueOf(gameEntity.status.name),
                        )
                    }
                    .toSet(),
                user1 =
                PublicUserDto(
                    username = matchEntity.player1.username,
                    name = matchEntity.player1.name,
                    tier = TierTypeDto.valueOf(matchEntity.player1.tier.name),
                    country = matchEntity.player1.country,
                    college = matchEntity.player1.college,
                    avatarId = matchEntity.player1.avatarId,
                ),
                user2 =
                PublicUserDto(
                    username = matchEntity.player2.username,
                    name = matchEntity.player2.name,
                    tier = TierTypeDto.valueOf(matchEntity.player2.tier.name),
                    country = matchEntity.player2.country,
                    college = matchEntity.player2.college,
                    avatarId = matchEntity.player2.avatarId,
                ),
            )
        }
    }

    private fun mapPvPMatchEntitiesToDtos(pvPMatchEntities: List<PvPMatchEntity>): List<PvPMatchDto> {
        return pvPMatchEntities.map { pvPMatchEntity ->
            PvPMatchDto(
                id = pvPMatchEntity.id,
                matchMode = MatchModeDto.valueOf(pvPMatchEntity.mode.name),
                matchVerdict = VerdictDto.valueOf(pvPMatchEntity.verdict.name),
                createdAt = pvPMatchEntity.createdAt,
                game =
                   PvPGameDto (
                        id = pvPMatchEntity.game.matchId,
                        scorePlayer1 = pvPMatchEntity.game.scorePlayer1,
                        scorePlayer2 = pvPMatchEntity.game.scorePlayer2,
                        status = PvPGameStatusDto.valueOf(pvPMatchEntity.game.status.name),
                   ),
                user1 =
                PublicUserDto(
                    username = pvPMatchEntity.player1.username,
                    name = pvPMatchEntity.player1.name,
                    tier = TierTypeDto.valueOf(pvPMatchEntity.player1.tier.name),
                    country = pvPMatchEntity.player1.country,
                    college = pvPMatchEntity.player1.college,
                    avatarId = pvPMatchEntity.player1.avatarId,
                ),
                user2 =
                PublicUserDto(
                    username = pvPMatchEntity.player2.username,
                    name = pvPMatchEntity.player2.name,
                    tier = TierTypeDto.valueOf(pvPMatchEntity.player2.tier.name),
                    country = pvPMatchEntity.player2.country,
                    college = pvPMatchEntity.player2.college,
                    avatarId = pvPMatchEntity.player2.avatarId,
                ),
            )
        }
    }

    private fun mapDailyChallengeMatchEntitiesToDtos(
        dailyChallengeMatchEntities: List<DailyChallengeMatchEntity>
    ): List<MatchDto> {
        return dailyChallengeMatchEntities.map { entity ->
            MatchDto(
                id = entity.id,
                matchMode = MatchModeDto.valueOf("DAILYCHALLENGE"),
                matchVerdict = VerdictDto.valueOf(entity.verdict.name),
                createdAt = entity.createdAt,
                games =
                setOf(
                    GameDto(
                        id = entity.game.id,
                        destruction = BigDecimal(entity.game.destruction),
                        coinsUsed = entity.game.coinsUsed,
                        status = GameStatusDto.valueOf(entity.game.status.name)
                    )
                ),
                user1 =
                PublicUserDto(
                    username = entity.user.username,
                    name = entity.user.name,
                    tier = TierTypeDto.valueOf(entity.user.tier.name),
                    country = entity.user.country,
                    college = entity.user.college,
                    avatarId = entity.user.avatarId,
                ),
            )
        }
    }

    fun getTopMatches(): List<Any> {
        val matches = matchRepository.findTop10ByOrderByTotalPointsDesc()
        val pvPMatches = pvPMatchRepository.findTop10ByOrderByTotalPointsDesc()
        return listOf(mapMatchEntitiesToDtos(matches) + mapPvPMatchEntitiesToDtos(pvPMatches))
    }

    fun getUserNormalMatches(userId: UUID, page: Int?, size: Int?): List<MatchDto> {
        val publicUser = publicUserService.getPublicUser(userId)
        val pageRequest =
            PageRequest.of(
                page ?: 0,
                size ?: 10,
                Sort.by(Sort.Order.desc("createdAt")),
            )

        val matches = matchRepository.findByPlayer1OrderByCreatedAtDesc(publicUser, pageRequest)

        return mapMatchEntitiesToDtos(matches)
    }

    fun getUserDCMatches(userId: UUID, page: Int?, size: Int?): List<MatchDto> {
        val publicUser = publicUserService.getPublicUser(userId)
        val pageRequest =
            PageRequest.of(
                page ?: 0,
                size ?: 10,
                Sort.by(Sort.Order.desc("createdAt")),
            )

        val dcMatches =
            dailyChallengeMatchRepository.findByUserOrderByCreatedAtDesc(publicUser).takeWhile {
                Duration.between(it.createdAt, Instant.now()).toHours() < 24 &&
                        it.verdict != DailyChallengeMatchVerdictEnum.STARTED
            }
        return mapDailyChallengeMatchEntitiesToDtos(dcMatches)
    }

    fun getUserPvPMatches(userId: UUID, page: Int?, size: Int?): List<PvPMatchDto> {
        val publicUser = publicUserService.getPublicUser(userId)
        val pageRequest =
            PageRequest.of(
                page ?: 0,
                size ?: 10,
                Sort.by(Sort.Order.desc("createdAt")),
            )
        val pvPMatches = pvPMatchRepository.findByPlayer1OrderByCreatedAtDesc(publicUser, pageRequest)
        return mapPvPMatchEntitiesToDtos(pvPMatches)
    }

    @RabbitListener(queues = ["gameStatusUpdateQueue"], ackMode = "AUTO")
    fun receiveGameResult(gameStatusUpdateJson: String) {
        val gameStatusUpdateEntity =
            mapper.readValue(gameStatusUpdateJson, GameStatusUpdateEntity::class.java)
        val gameId = gameStatusUpdateEntity.gameId

        val matchId: UUID
        if(gameRepository.findById(gameId).isPresent) {
            val game = gameRepository.findById(gameId).get()
            matchId = game.matchId // for normal matches, each match has 2 games
        }
        else if(pvPGameRepository.findById(gameId).isPresent) {
            matchId = gameId // for pvp matches, matchId is same as gameId
        }
        else {
            throw CustomException(HttpStatus.NOT_FOUND, "Game not found")
        }

        if (matchRepository.findById(matchId).isPresent) {
            val updatedGame = gameService.updateGameStatus(gameStatusUpdateJson)
            val match = matchRepository.findById(updatedGame.matchId).get()
            if (match.mode != MatchModeEnum.AUTO && match.games.first().id == updatedGame.id) {
                simpMessagingTemplate.convertAndSend(
                    "/updates/${match.player1.userId}",
                    mapper.writeValueAsString(
                        GameDto(
                            id = updatedGame.id,
                            destruction = BigDecimal(updatedGame.destruction),
                            coinsUsed = updatedGame.coinsUsed,
                            status = GameStatusDto.valueOf(updatedGame.status.name),
                        )
                    )
                )
            }
            if (match.mode != MatchModeEnum.SELF &&
                match.games.all { game ->
                    game.status == GameStatusEnum.EXECUTED || game.status == GameStatusEnum.EXECUTE_ERROR
                }
            ) {

                if (match.mode == MatchModeEnum.AUTO) {
                    if (match.games.any { game -> game.status == GameStatusEnum.EXECUTE_ERROR }) {
                        val autoMatch = autoMatchRepository.findById(match.id).get()
                        if (autoMatch.tries < 2) {
                            autoMatchRepository.delete(autoMatch)
                            val newMatchId =
                                createDualMatch(match.player1.userId, match.player2.username, MatchModeEnum.AUTO)
                            autoMatchRepository.save(AutoMatchEntity(newMatchId, autoMatch.tries + 1))
                            return
                        }
                    }
                }

                val player1Game = match.games.first()
                val player2Game = match.games.last()
                val verdict =
                    verdictAlgorithm.getVerdict(
                        player1Game.status == GameStatusEnum.EXECUTE_ERROR,
                        player1Game.coinsUsed,
                        player1Game.destruction,
                        player2Game.status == GameStatusEnum.EXECUTE_ERROR,
                        player2Game.coinsUsed,
                        player2Game.destruction
                    )
                val finishedMatch = match.copy(verdict = verdict)
                val (newUserRating, newOpponentRating) =
                    ratingHistoryService.updateRating(match.player1.userId, match.player2.userId, verdict)
                if (match.mode == MatchModeEnum.MANUAL) {
                    if ((
                        match.player1.tier == TierTypeDto.TIER2 &&
                            match.player2.tier == TierTypeDto.TIER2
                        ) ||
                        (
                            match.player1.tier == TierTypeDto.TIER_PRACTICE &&
                                match.player2.tier == TierTypeDto.TIER_PRACTICE
                            )
                    ) {
                        publicUserService.updatePublicRating(
                            userId = match.player1.userId,
                            isInitiator = true,
                            verdict = verdict,
                            newRating = newUserRating
                        )
                        publicUserService.updatePublicRating(
                            userId = match.player2.userId,
                            isInitiator = false,
                            verdict = verdict,
                            newRating = newOpponentRating
                        )
                    }
                    notificationService.sendNotification(
                        match.player1.userId,
                        "Match Result",
                        "${
                        when (verdict) {
                            MatchVerdictEnum.PLAYER1 -> "Won"
                            MatchVerdictEnum.PLAYER2 -> "Lost"
                            MatchVerdictEnum.TIE -> "Tied"
                        }
                        } against ${match.player2.username}",
                    )
                }
                matchRepository.save(finishedMatch)

                if (match.mode == MatchModeEnum.AUTO) {
                    if (autoMatchRepository.findAll().all { autoMatch ->
                        matchRepository.findById(autoMatch.matchId).get().games.all { game ->
                            game.status == GameStatusEnum.EXECUTED || game.status == GameStatusEnum.EXECUTE_ERROR
                        }
                    }
                    ) {
                        val matches =
                            matchRepository.findByIdIn(autoMatchRepository.findAll().map { it.matchId })
                        val userIds =
                            matches.map { it.player1.userId }.toSet() +
                                matches.map { it.player2.userId }.toSet()
                        val (userIdWinMap, userIdLossMap, userIdTieMap) =
                            ratingHistoryService.updateTotalWinsTiesLosses(
                                userIds = userIds.toList(), matches = matches
                            )
                        publicUserService.updateAutoMatchWinsLosses(
                            userIds.toList(), userIdWinMap, userIdLossMap, userIdTieMap
                        )
                        val newRatings =
                            ratingHistoryService.updateAndGetAutoMatchRatings(userIds.toList(), matches)
                        newRatings.forEach { (userId, newRating) ->
                            publicUserService.updateAutoMatchRating(userId = userId, newRating = newRating.rating)
                        }
                        logger.info("LeaderBoard Tier Promotion and Demotion started")
                        publicUserService.promoteTiers()
                    }
                    notificationService.sendNotification(
                        match.player1.userId,
                        "Auto Match Result",
                        "${
                        when (verdict) {
                            MatchVerdictEnum.PLAYER1 -> "Won"
                            MatchVerdictEnum.PLAYER2 -> "Lost"
                            MatchVerdictEnum.TIE -> "Tied"
                        }
                        } against ${match.player2.username}",
                    )
                    logger.info(
                        "Match between ${match.player1.username} and ${match.player2.username} completed with verdict $verdict"
                    )
                }
            }
        } else if (dailyChallengeMatchRepository.findById(matchId).isPresent) {
            val updatedGame = gameService.updateGameStatus(gameStatusUpdateJson)
            val match = dailyChallengeMatchRepository.findById(matchId).get()
            simpMessagingTemplate.convertAndSend(
                "/updates/${match.user.userId}",
                mapper.writeValueAsString(
                    GameDto(
                        id = updatedGame.id,
                        destruction = BigDecimal(updatedGame.destruction),
                        coinsUsed = updatedGame.coinsUsed,
                        status = GameStatusDto.valueOf(updatedGame.status.name),
                    )
                )
            )
            if (updatedGame.status != GameStatusEnum.EXECUTING) {
                val updatedMatch =
                    match.copy(
                        verdict =
                        dailyChallengeService.completeDailyChallenge(updatedGame, match.user.userId)
                    )
                notificationService.sendNotification(
                    match.user.userId,
                    title = "Daily Challenge Results",
                    content =
                    when (updatedMatch.verdict) {
                        DailyChallengeMatchVerdictEnum.SUCCESS -> "Successfully completed challenge"
                        DailyChallengeMatchVerdictEnum.FAILURE -> "Failed to complete challenge"
                        else -> {
                            "Some error occurred. Try again!"
                        }
                    }
                )
                dailyChallengeMatchRepository.save(updatedMatch)
            }
        } else if(pvPMatchRepository.findById(matchId).isPresent) {
            val updatedGame = pvPGameService.updateGameStatus(gameStatusUpdateJson)
            val match = pvPMatchRepository.findById(updatedGame.matchId).get()
            if (match.game.matchId == updatedGame.matchId) {
                simpMessagingTemplate.convertAndSend(
                    "/updates/${match.player1.userId}",
                    mapper.writeValueAsString(
                        PvPGameDto(
                            id = updatedGame.matchId,
                            scorePlayer1 = updatedGame.scorePlayer1,
                            scorePlayer2 = updatedGame.scorePlayer2,
                            status = PvPGameStatusDto.valueOf(updatedGame.status.name),
                        )
                    )
                )
            }
            if (match.game.status == PvPGameStatusEnum.EXECUTED) {
                val verdict =
                    verdictAlgorithm.getPvPVerdict(
                        match.game.status == PvPGameStatusEnum.EXECUTE_ERROR,
                        match.game.scorePlayer1,
                        match.game.status == PvPGameStatusEnum.EXECUTE_ERROR,
                        match.game.scorePlayer2,
                    )
                val finishedMatch = match.copy(verdict = verdict)
                val (newUserRating, newOpponentRating) =
                    ratingHistoryService.updateRating(match.player1.userId, match.player2.userId, verdict)

                publicUserService.updatePublicRating(
                    userId = match.player1.userId,
                    isInitiator = true,
                    verdict = verdict,
                    newRating = newUserRating
                )

                publicUserService.updatePublicRating(
                    userId = match.player2.userId,
                    isInitiator = false,
                    verdict = verdict,
                    newRating = newOpponentRating
                )

                pvPMatchRepository.save(finishedMatch)
                notificationService.sendNotification(
                    match.player1.userId,
                    "Match Result",
                    "${
                        when (verdict) {
                            MatchVerdictEnum.PLAYER1 -> "Won"
                            MatchVerdictEnum.PLAYER2 -> "Lost"
                            MatchVerdictEnum.TIE -> "Tied"
                        }
                    } against ${match.player2.username}",
                )
            }
        }
    }
}
