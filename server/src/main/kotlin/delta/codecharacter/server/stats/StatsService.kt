package delta.codecharacter.server.stats

import delta.codecharacter.dtos.UserMatchStatsInnerDto
import delta.codecharacter.server.daily_challenge.DailyChallengeService
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.user.public_user.PublicUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.min

@Service
class StatsService(@Autowired private val statsRepository:StatsRepository,
                    @Autowired private val publicUserRepository: PublicUserRepository){

    @Value("\${environment.event-start-date}") private lateinit var startDate: String
    fun findNumberOfDays(): Int {
        val givenDateTime = Instant.parse(startDate)
        val nowDateTime = Instant.now()
        val period: Duration = Duration.between(givenDateTime, nowDateTime)
        return period.toDays().toInt()
    }
    fun updateStats(
        userId: UUID,
        atkDmg: Double,
        coins: Int,
        dcAttempts: Int,
        verdict: DailyChallengeMatchVerdictEnum?,
    ) {
        val currentDay = findNumberOfDays()
        if(statsRepository.existsById(userId)) {
            val user = (statsRepository.findById(userId).get())
            val userStats:StatEntity;
            if(user.stats.containsKey(currentDay)){
               userStats = user.stats[currentDay]!!
            } else {
                var win  = 0
                if(dcAttempts!=0){
                    win = if(verdict == DailyChallengeMatchVerdictEnum.SUCCESS) 1 else 0
                }
                user.stats[currentDay] = StatEntity(atkDmg,win,coins, Instant.now());
                userStats = user.stats[currentDay]!!
            }
            val updatedUserStat =
                userStats.copy(
                    avgAtk = (userStats.avgAtk * user.stats.size  + atkDmg) / (1+user.stats.size),
                    coins = (userStats.coins * user.stats.size + coins) / (1+user.stats.size),
                    dc_wins =
                    if (dcAttempts != 0 && verdict == DailyChallengeMatchVerdictEnum.SUCCESS)
                        userStats.dc_wins + 1
                    else userStats.dc_wins,
                )
            user.stats[currentDay] = updatedUserStat;
            statsRepository.save(user)
        } else {
            val initialMap = HashMap<Int,StatEntity>();
            var win  = 0
            if(dcAttempts!=0){
                win = if(verdict == DailyChallengeMatchVerdictEnum.SUCCESS) 1 else 0
            }
            initialMap[currentDay] = StatEntity(atkDmg,win,coins,Instant.now())
            statsRepository.insert(PublicStatEntity(userId,initialMap))
        }
    }
    fun getInfo(userId: UUID): List<List<UserMatchStatsInnerDto>> {
        val l =  ArrayList<List<UserMatchStatsInnerDto>>();
        val pageRequest =
            PageRequest.of(
                 0,
                 10,
                Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("wins"), Sort.Order.asc("username"))
            )
        val topUserUUID = publicUserRepository.findAll(pageRequest).get().findFirst().get().userId
        val topUser =  statsRepository.findById(topUserUUID).get()
        var lTop = convertToDTO(topUser)
        val userExists =  statsRepository.existsById(userId)
        if(!userExists || (userId == topUserUUID)){
            l.add(lTop)
            return  l
        }
        val currentUserStats =  statsRepository.findById(userId).get()
        var lUser = convertToDTO(currentUserStats)

        val minEle = min(lUser.size,lTop.size)
        if(lUser.size < lTop.size){
           lTop = lTop.slice(IntRange(lTop.size-minEle,lTop.size))
        } else if (lUser.size > lTop.size) {
            lUser = lUser.slice(IntRange(lUser.size-minEle,lUser.size))
        }
        l.add(lUser)
        l.add(lTop)
        return  l
    }

    private fun convertToDTO(currentUserStatsArr: PublicStatEntity): List<UserMatchStatsInnerDto> {
        return currentUserStatsArr.stats.values.map {it ->
             UserMatchStatsInnerDto(
                avgAtk = BigDecimal(it.avgAtk),
                dcWins = BigDecimal(it.dc_wins),
                coins = BigDecimal(it.coins)
            )
        }
    }

}
