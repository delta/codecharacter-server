package delta.codecharacter.server.user.rating_history

import delta.codecharacter.dtos.RatingHistoryDto
import delta.codecharacter.server.logic.rating.GlickoRating
import delta.codecharacter.server.logic.rating.RatingAlgorithm
import delta.codecharacter.server.match.MatchEntity
import delta.codecharacter.server.match.MatchVerdictEnum
import delta.codecharacter.server.match.PvPMatchEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class RatingHistoryService(
    @Autowired private val ratingHistoryRepository: RatingHistoryRepository,
    @Autowired private val ratingAlgorithm: RatingAlgorithm
) {
    fun create(userId: UUID) {
        val ratingHistoryNormal =
            RatingHistoryEntity(
                userId = userId, rating = 1500.0, ratingDeviation = 350.0, validFrom = Instant.now(), ratingType = RatingType.NORMAL
            )
        val ratingHistoryPvP =
            RatingHistoryEntity(
                userId = userId, rating = 1500.0, ratingDeviation = 350.0, validFrom = Instant.now(), ratingType = RatingType.PVP
            )
        ratingHistoryRepository.save(ratingHistoryNormal)
        ratingHistoryRepository.save(ratingHistoryPvP)
    }

    fun getRatingHistory(userId: UUID, ratingType:RatingType): List<RatingHistoryDto> {
        return ratingHistoryRepository.findAllByUserIdAndRatingType(userId,ratingType).map {
            RatingHistoryDto(
                rating = BigDecimal(it.rating),
                ratingDeviation = BigDecimal(it.ratingDeviation),
                validFrom = it.validFrom
            )
        }
    }

    private fun convertVerdictToMatchResult(verdict: MatchVerdictEnum): Double {
        return when (verdict) {
            MatchVerdictEnum.PLAYER1 -> 1.0
            MatchVerdictEnum.PLAYER2 -> 0.0
            MatchVerdictEnum.TIE -> 0.5
        }
    }

    fun updateRating(
        userId: UUID,
        opponentId: UUID,
        verdict: MatchVerdictEnum,
        ratingType: RatingType
    ): Pair<Double, Double> {
        val (_, userRating, userRatingDeviation, userRatingValidFrom) =
            ratingHistoryRepository.findFirstByUserIdAndRatingTypeOrderByValidFromDesc(userId,ratingType)
        val (_, opponentRating, opponentRatingDeviation, opponentRatingValidFrom) =
            ratingHistoryRepository.findFirstByUserIdAndRatingTypeOrderByValidFromDesc(opponentId,ratingType)

        val userWeightedRatingDeviation =
            ratingAlgorithm.getWeightedRatingDeviationSinceLastCompetition(
                userRatingDeviation, userRatingValidFrom
            )
        val opponentWeightedRatingDeviation =
            ratingAlgorithm.getWeightedRatingDeviationSinceLastCompetition(
                opponentRatingDeviation, opponentRatingValidFrom
            )

        val newUserRating =
            ratingAlgorithm.calculateNewRating(
                GlickoRating(userRating, userWeightedRatingDeviation),
                listOf(GlickoRating(opponentRating, opponentWeightedRatingDeviation)),
                listOf(convertVerdictToMatchResult(verdict))
            )
        val newOpponentRating =
            ratingAlgorithm.calculateNewRating(
                GlickoRating(opponentRating, opponentWeightedRatingDeviation),
                listOf(GlickoRating(userRating, userWeightedRatingDeviation)),
                listOf(1.0 - convertVerdictToMatchResult(verdict))
            )

        val currentInstant = Instant.now()
        ratingHistoryRepository.save(
            RatingHistoryEntity(
                userId = userId,
                rating = newUserRating.rating,
                ratingDeviation = newUserRating.ratingDeviation,
                validFrom = currentInstant,
                ratingType = ratingType
            )
        )
        ratingHistoryRepository.save(
            RatingHistoryEntity(
                userId = opponentId,
                rating = newOpponentRating.rating,
                ratingDeviation = newOpponentRating.ratingDeviation,
                validFrom = currentInstant,
                ratingType = ratingType
            )
        )

        return Pair(newUserRating.rating, newOpponentRating.rating)
    }

    private fun getNewRatingAfterAutoMatches(
        userId: UUID,
        userRatings: Map<UUID, RatingHistoryEntity>,
        autoMatches: List<MatchEntity>
    ): GlickoRating {
        val userAsInitiatorMatches = autoMatches.filter { it.player1.userId == userId }
        val userAsOpponentMatches = autoMatches.filter { it.player2.userId == userId }

        val usersWeightedRatingDeviations =
            userRatings
                .map {
                    it.key to
                        ratingAlgorithm.getWeightedRatingDeviationSinceLastCompetition(
                            it.value.ratingDeviation, it.value.validFrom
                        )
                }
                .toMap()

        val ratingsForUserAsPlayer1 =
            userAsInitiatorMatches.map { match ->
                GlickoRating(match.player2.rating, usersWeightedRatingDeviations[match.player2.userId]!!)
            }
        val verdictsForUserAsPlayer1 =
            userAsInitiatorMatches.map { match -> convertVerdictToMatchResult(match.verdict) }

        val ratingsForUserAsPlayer2 =
            userAsOpponentMatches.map { match ->
                GlickoRating(match.player1.rating, usersWeightedRatingDeviations[match.player1.userId]!!)
            }
        val verdictsForUserAsPlayer2 =
            userAsOpponentMatches.map { match -> 1.0 - convertVerdictToMatchResult(match.verdict) }

        val ratings = ratingsForUserAsPlayer1 + ratingsForUserAsPlayer2
        val verdicts = verdictsForUserAsPlayer1 + verdictsForUserAsPlayer2

        return ratingAlgorithm.calculateNewRating(
            GlickoRating(userRatings[userId]!!.rating, usersWeightedRatingDeviations[userId]!!),
            ratings,
            verdicts
        )
    }

    private fun getNewRatingAfterPvPAutoMatches(
        userId: UUID,
        userRatings: Map<UUID, RatingHistoryEntity>,
        pvPAutoMatches: List<PvPMatchEntity>
    ): GlickoRating {
        val userAsPlayer1Matches = pvPAutoMatches.filter { it.player1.userId == userId }
        val userAsPlayer2Matches = pvPAutoMatches.filter { it.player2.userId == userId }

        val usersWeightedRatingDeviations =
            userRatings
                .map {
                    it.key to
                        ratingAlgorithm.getWeightedRatingDeviationSinceLastCompetition(
                            it.value.ratingDeviation, it.value.validFrom
                        )
                }
                .toMap()

        val ratingsForUserAsPlayer1 =
            userAsPlayer1Matches.map { match ->
                GlickoRating(match.player2.pvpRating, usersWeightedRatingDeviations[match.player2.userId]!!)
            }
        val verdictsForUserAsPlayer1 =
            userAsPlayer1Matches.map { match -> convertVerdictToMatchResult(match.verdict) }

        val ratingsForUserAsPlayer2 =
            userAsPlayer2Matches.map { match ->
                GlickoRating(match.player1.pvpRating, usersWeightedRatingDeviations[match.player1.userId]!!)
            }
        val verdictsForUserAsPlayer2 =
            userAsPlayer2Matches.map { match -> 1.0 - convertVerdictToMatchResult(match.verdict) }

        val ratings = ratingsForUserAsPlayer1 + ratingsForUserAsPlayer2
        val verdicts = verdictsForUserAsPlayer1 + verdictsForUserAsPlayer2

        return ratingAlgorithm.calculateNewRating(
            GlickoRating(userRatings[userId]!!.rating, usersWeightedRatingDeviations[userId]!!),
            ratings,
            verdicts
        )
    }

    fun updateTotalWinsTiesLosses(
        userIds: List<UUID>,
        matches: List<MatchEntity>
    ): Triple<Map<UUID, Int>, Map<UUID, Int>, Map<UUID, Int>> {
        val userIdWinMap = userIds.associateWith { 0 }.toMutableMap()
        val userIdLossMap = userIds.associateWith { 0 }.toMutableMap()
        val userIdTieMap = userIds.associateWith { 0 }.toMutableMap()
        matches.forEach { match ->
            when (match.verdict) {
                MatchVerdictEnum.PLAYER1 -> {
                    userIdWinMap[match.player1.userId] = userIdWinMap[match.player1.userId]!! + 1
                    userIdLossMap[match.player2.userId] = userIdLossMap[match.player2.userId]!! + 1
                }
                MatchVerdictEnum.PLAYER2 -> {
                    userIdWinMap[match.player2.userId] = userIdWinMap[match.player2.userId]!! + 1
                    userIdLossMap[match.player1.userId] = userIdLossMap[match.player1.userId]!! + 1
                }
                MatchVerdictEnum.TIE -> {
                    userIdTieMap[match.player1.userId] = userIdTieMap[match.player1.userId]!! + 1
                    userIdTieMap[match.player2.userId] = userIdTieMap[match.player2.userId]!! + 1
                }
            }
        }
        return Triple(userIdWinMap.toMap(), userIdLossMap.toMap(), userIdTieMap.toMap())
    }

    fun updateTotalWinsTiesLossesPvP(
        userIds: List<UUID>,
        matches: List<PvPMatchEntity>
    ): Triple<Map<UUID, Int>, Map<UUID, Int>, Map<UUID, Int>> {
        val userIdWinMap = userIds.associateWith { 0 }.toMutableMap()
        val userIdLossMap = userIds.associateWith { 0 }.toMutableMap()
        val userIdTieMap = userIds.associateWith { 0 }.toMutableMap()
        matches.forEach { match ->
            when (match.verdict) {
                MatchVerdictEnum.PLAYER1 -> {
                    userIdWinMap[match.player1.userId] = userIdWinMap[match.player1.userId]!! + 1
                    userIdLossMap[match.player2.userId] = userIdLossMap[match.player2.userId]!! + 1
                }
                MatchVerdictEnum.PLAYER2 -> {
                    userIdWinMap[match.player2.userId] = userIdWinMap[match.player2.userId]!! + 1
                    userIdLossMap[match.player1.userId] = userIdLossMap[match.player1.userId]!! + 1
                }
                MatchVerdictEnum.TIE -> {
                    userIdTieMap[match.player1.userId] = userIdTieMap[match.player1.userId]!! + 1
                    userIdTieMap[match.player2.userId] = userIdTieMap[match.player2.userId]!! + 1
                }
            }
        }
        return Triple(userIdWinMap.toMap(), userIdLossMap.toMap(), userIdTieMap.toMap())
    }

    fun updateAndGetAutoMatchRatings(
        userIds: List<UUID>,
        matches: List<MatchEntity>
    ): Map<UUID, GlickoRating> {
        val userRatings =
            userIds.associateWith { userId ->
                ratingHistoryRepository.findFirstByUserIdAndRatingTypeOrderByValidFromDesc(userId,RatingType.NORMAL)
            }
        val newRatings =
            userIds.associateWith { userId ->
                getNewRatingAfterAutoMatches(userId, userRatings, matches)
            }
        val currentInstant = Instant.now()
        newRatings.forEach { (userId, rating) ->
            ratingHistoryRepository.save(
                RatingHistoryEntity(
                    userId = userId,
                    rating = rating.rating,
                    ratingDeviation = rating.ratingDeviation,
                    validFrom = currentInstant,
                    ratingType = RatingType.NORMAL
                )
            )
        }
        return newRatings
    }

    fun updateAndGetPvPAutoMatchRatings(
        userIds: List<UUID>,
        matches: List<PvPMatchEntity>
    ): Map<UUID, GlickoRating> {
        val userRatings =
            userIds.associateWith { userId ->
                ratingHistoryRepository.findFirstByUserIdAndRatingTypeOrderByValidFromDesc(userId,RatingType.PVP)
            }
        val newRatings =
            userIds.associateWith { userId ->
                getNewRatingAfterPvPAutoMatches(userId, userRatings, matches)
            }
        val currentInstant = Instant.now()
        newRatings.forEach { (userId, rating) ->
            ratingHistoryRepository.save(
                RatingHistoryEntity(
                    userId = userId,
                    rating = rating.rating,
                    ratingDeviation = rating.ratingDeviation,
                    validFrom = currentInstant,
                    ratingType = RatingType.PVP
                )
            )
        }
        return newRatings
    }
}
