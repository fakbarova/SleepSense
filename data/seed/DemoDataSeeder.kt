package com.circadianx.sleepsense.data.seed

import com.circadianx.sleepsense.data.local.db.dao.ChallengeDao
import com.circadianx.sleepsense.data.local.db.dao.RoutineDao
import com.circadianx.sleepsense.data.local.db.dao.SleepRecordDao
import com.circadianx.sleepsense.data.local.db.dao.SocialDao
import com.circadianx.sleepsense.data.local.db.dao.StepDao
import com.circadianx.sleepsense.data.local.db.entity.ChallengeCheckInEntity
import com.circadianx.sleepsense.data.local.db.entity.ChallengeEntity
import com.circadianx.sleepsense.data.local.db.entity.GroupChallengeEntity
import com.circadianx.sleepsense.data.local.db.entity.GroupMemberEntity
import com.circadianx.sleepsense.data.local.db.entity.RoutineCompletionEntity
import com.circadianx.sleepsense.data.local.db.entity.RoutineItemEntity
import com.circadianx.sleepsense.data.local.db.entity.SleepRecordEntity
import com.circadianx.sleepsense.data.local.db.entity.StepDayEntity
import com.circadianx.sleepsense.data.local.db.entity.StoryEntity
import com.circadianx.sleepsense.domain.repository.BedtimeSchedule
import com.circadianx.sleepsense.domain.repository.UserPreferencesRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoDataSeeder @Inject constructor(
    private val sleepRecordDao: SleepRecordDao,
    private val stepDao: StepDao,
    private val challengeDao: ChallengeDao,
    private val routineDao: RoutineDao,
    private val socialDao: SocialDao,
    private val prefs: UserPreferencesRepository
) {
    suspend fun seed() {
        val today = LocalDate.now()
        val nowMs = System.currentTimeMillis()

        seedSleepRecords(today)
        seedSteps(today, nowMs)
        seedRoutines(today, nowMs)
        seedChallenges(today, nowMs)
        seedSocial(nowMs)

        prefs.setBedtimeSchedule(BedtimeSchedule(targetBedtimeMinutes = 23 * 60, targetWakeMinutes = 7 * 60))
        prefs.setPrimaryGoals(setOf("Improve sleep quality", "Build exercise habit"))
        prefs.setOnboardingCompleted(true)
    }

    private suspend fun seedSleepRecords(today: LocalDate) {
        val scores = listOf(82, 88, 79, 91, 76, 84, 87, 72, 68, 94, 58, 81, 74, 86)
        val durationsMinutes = listOf(432, 468, 421, 489, 398, 446, 455, 384, 372, 476, 320, 438, 407, 462)
        val disturbances = listOf(2, 1, 3, 0, 4, 2, 1, 4, 5, 0, 6, 2, 3, 1)
        val bedtimeOffsets = listOf(0, 15, -10, 25, 40, -20, 10, 55, 35, -5, 70, 20, 45, 5)

        scores.indices.forEach { index ->
            val night = today.minusDays(index.toLong() + 1)
            val start = night
                .atTime(LocalTime.of(22, 45))
                .plusMinutes(bedtimeOffsets[index].toLong())
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val end = start + durationsMinutes[index] * 60_000L

            sleepRecordDao.insert(
                SleepRecordEntity(
                    id = DEMO_SLEEP_ID_BASE + index,
                    startTimeMs = start,
                    endTimeMs = end,
                    disturbanceCount = disturbances[index],
                    sleepScore = scores[index]
                )
            )
        }
    }

    private suspend fun seedSteps(today: LocalDate, nowMs: Long) {
        val steps = listOf(8600, 10250, 7400, 11800, 6900, 5300, 4100, 9600, 11100, 7800, 3400, 8200, 6500, 9000)
        steps.forEachIndexed { index, count ->
            val day = today.minusDays(index.toLong())
            stepDao.upsert(
                StepDayEntity(
                    epochDay = day.toEpochDay(),
                    steps = count,
                    updatedAtMs = nowMs - index * DAY_MS
                )
            )
        }
    }

    private suspend fun seedRoutines(today: LocalDate, nowMs: Long) {
        val items = listOf(
            RoutineItemEntity(id = DEMO_ROUTINE_ID_BASE, type = "pre_sleep", title = "Brush teeth", reminderMinutesOfDay = 22 * 60),
            RoutineItemEntity(id = DEMO_ROUTINE_ID_BASE + 1, type = "pre_sleep", title = "Drink water", reminderMinutesOfDay = 22 * 60 + 10),
            RoutineItemEntity(id = DEMO_ROUTINE_ID_BASE + 2, type = "pre_sleep", title = "Light stretching (3-5 min)", reminderMinutesOfDay = 22 * 60 + 30),
            RoutineItemEntity(id = DEMO_ROUTINE_ID_BASE + 3, type = "morning", title = "Hydration", reminderMinutesOfDay = 7 * 60 + 10),
            RoutineItemEntity(id = DEMO_ROUTINE_ID_BASE + 4, type = "morning", title = "Short walk (10 min)", reminderMinutesOfDay = 7 * 60 + 40)
        )
        routineDao.insertItems(items)

        (0..6).forEach { dayOffset ->
            val epochDay = today.minusDays(dayOffset.toLong()).toEpochDay()
            val completedIds = when (dayOffset) {
                0 -> items.map { it.id }
                1 -> items.take(4).map { it.id }
                2 -> listOf(items[0].id, items[1].id, items[3].id)
                3 -> items.take(5).map { it.id }
                4 -> listOf(items[0].id, items[2].id, items[3].id)
                5 -> items.take(4).map { it.id }
                else -> listOf(items[0].id, items[3].id)
            }

            completedIds.forEach { itemId ->
                routineDao.insertCompletion(
                    RoutineCompletionEntity(
                        itemId = itemId,
                        epochDay = epochDay,
                        completedAtMs = nowMs - dayOffset * DAY_MS
                    )
                )
            }
        }
    }

    private suspend fun seedChallenges(today: LocalDate, nowMs: Long) {
        val challenges = listOf(
            ChallengeEntity(DEMO_CHALLENGE_ID_BASE, "7 nights before midnight", "sleep", 7, "In bed before 23:30", today.minusDays(7).toEpochDay(), nowMs - 7 * DAY_MS),
            ChallengeEntity(DEMO_CHALLENGE_ID_BASE + 1, "Evening walk streak", "exercise", 14, "Walk at least 20 minutes", today.minusDays(14).toEpochDay(), nowMs - 14 * DAY_MS),
            ChallengeEntity(DEMO_CHALLENGE_ID_BASE + 2, "No screens after 22:00", "screen_time", 7, "Avoid bedtime phone use", today.minusDays(5).toEpochDay(), nowMs - 5 * DAY_MS),
            ChallengeEntity(DEMO_CHALLENGE_ID_BASE + 3, "Consistent wake time", "sleep", 10, "Wake within a 30-minute window", today.minusDays(10).toEpochDay(), nowMs - 10 * DAY_MS),
            ChallengeEntity(DEMO_CHALLENGE_ID_BASE + 4, "10k step push", "exercise", 7, "Reach 10,000 steps", today.minusDays(3).toEpochDay(), nowMs - 3 * DAY_MS)
        )

        challenges.forEach { challenge ->
            challengeDao.insertChallenge(challenge)
            (0 until minOf(7, challenge.durationDays)).forEach { dayOffset ->
                val completed = (dayOffset + challenge.id.toInt()) % 3 != 0
                challengeDao.upsertCheckIn(
                    ChallengeCheckInEntity(
                        challengeId = challenge.id,
                        epochDay = today.minusDays(dayOffset.toLong()).toEpochDay(),
                        completed = completed,
                        updatedAtMs = nowMs - dayOffset * DAY_MS
                    )
                )
            }
        }
    }

    private suspend fun seedSocial(nowMs: Long) {
        val groups = listOf(
            GroupChallengeEntity(DEMO_GROUP_ID_BASE, "Better Sleep Circle", nowMs - 6 * DAY_MS),
            GroupChallengeEntity(DEMO_GROUP_ID_BASE + 1, "Morning Walk Crew", nowMs - 12 * DAY_MS),
            GroupChallengeEntity(DEMO_GROUP_ID_BASE + 2, "Screen-Free Nights", nowMs - 3 * DAY_MS)
        )

        groups.forEach { group ->
            socialDao.insertGroup(group)
            listOf("Shohjahon", "Aziza", "Timur").forEachIndexed { index, member ->
                socialDao.insertMember(
                    GroupMemberEntity(
                        groupId = group.id,
                        memberName = member,
                        joinedAtMs = group.createdAtMs + index * 3_600_000L
                    )
                )
            }
        }

        listOf(
            "Best sleep score this month" to "Hit 94/100 after a long walk and no screens before bed.",
            "Kept the bedtime streak alive" to "Five nights in a row before midnight.",
            "Quiet night" to "Only one disturbance recorded last night.",
            "Morning walk helped" to "Felt more awake after a short walk before work."
        ).forEachIndexed { index, story ->
            socialDao.insertStory(
                StoryEntity(
                    id = DEMO_STORY_ID_BASE + index,
                    title = story.first,
                    body = story.second,
                    createdAtMs = nowMs - index * 18 * 3_600_000L
                )
            )
        }
    }

    private companion object {
        const val DEMO_SLEEP_ID_BASE = 10_000L
        const val DEMO_ROUTINE_ID_BASE = 20_000L
        const val DEMO_CHALLENGE_ID_BASE = 30_000L
        const val DEMO_GROUP_ID_BASE = 40_000L
        const val DEMO_STORY_ID_BASE = 50_000L
        const val DAY_MS = 86_400_000L
    }
}
