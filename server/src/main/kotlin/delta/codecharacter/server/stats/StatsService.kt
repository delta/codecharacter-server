package delta.codecharacter.server.stats

import delta.codecharacter.dtos.UserMatchStatsInnerDto
import delta.codecharacter.server.daily_challenge.match.DailyChallengeMatchVerdictEnum
import delta.codecharacter.server.user.public_user.PublicUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

@Service
class StatsService(@Autowired private val statsRepository:StatsRepository,
                    @Autowired private val publicUserRepository: PublicUserRepository){
    fun updateStats(
        userId: UUID,
        atkDmg: Double,
        coins: Int,
        dcAttempts: Int,
        verdict: DailyChallengeMatchVerdictEnum?,
    ) {
        val DAYS = 15;
        if(statsRepository.existsById(userId)) {
            val userList = statsRepository.findById(userId).get()
            val user = userList.stats.last();
            val updatedUserStat =
                user.copy(
                    maxAtk = max(user.maxAtk, atkDmg),
                    minAtk = min(user.minAtk, atkDmg),
                    avgAtk = (user.avgAtk * 2 + atkDmg) / 2,
                    coins = (user.coins * 2 + coins) / 2,
                    dc_completions = (user.dc_completions +
                            if (dcAttempts != 0 && verdict == DailyChallengeMatchVerdictEnum.SUCCESS) 1
                            else 0),
                    dc_losses =
                    if (dcAttempts != 0 && verdict == DailyChallengeMatchVerdictEnum.FAILURE)
                        user.dc_losses + 1
                    else user.dc_losses,
                    dc_wins =
                    if (dcAttempts != 0 && verdict == DailyChallengeMatchVerdictEnum.SUCCESS)
                        user.dc_wins + 1
                    else user.dc_wins,
                )
            val updatedUserList = (userList.stats.toMutableList())
            if(updatedUserList.size < DAYS) {
                updatedUserList.add(updatedUserStat)
            } else {
                updatedUserList.removeAt(0)
                updatedUserList.add(updatedUserStat)
            }
            val updatedUser = userList.copy(stats = updatedUserList)
            statsRepository.save(updatedUser);
        } else {
            statsRepository.insert((PublicStatEntity(userId,listOf(StatEntity(0.0,0.0,0.0,0,0,0,0,0)))))
        }
    }
    fun getInfo(userId: UUID): List<List<UserMatchStatsInnerDto>> {
        if(!statsRepository.existsById(userId)){
            statsRepository.insert(PublicStatEntity(userId,listOf(StatEntity(0.0,0.0,0.0,0,0,0,0,0))))
        }
        val l =  ArrayList<List<UserMatchStatsInnerDto>>();
        val currentUserStats =  statsRepository.findById(userId).get();
        val pageRequest =
            PageRequest.of(
                 0,
                 10,
                Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("wins"), Sort.Order.asc("username"))
            )
        val topUserUUID = publicUserRepository.findAll(pageRequest).get().findFirst().get().userId
        val topUser =  statsRepository.findById(topUserUUID).get()


        l.add(convertToDTO(currentUserStats))
        l.add(convertToDTO(topUser));
        return  l;
    }

    private fun convertToDTO(currentUserStatsArr: PublicStatEntity): List<UserMatchStatsInnerDto> {
        return currentUserStatsArr.stats.map {it ->
             UserMatchStatsInnerDto(
                maxAtk = BigDecimal(it.maxAtk),
                minAtk = BigDecimal(it.minAtk),
                avgAtk = BigDecimal(it.avgAtk),
                dcWins = BigDecimal(it.dc_wins),
                dcLosses = BigDecimal(it.dc_losses),
                dcCompletions = BigDecimal(it.dc_completions),
                dcDestruction = BigDecimal(it.dc_destruction),
                coins = BigDecimal(it.coins)
            )
        }
    }

}
