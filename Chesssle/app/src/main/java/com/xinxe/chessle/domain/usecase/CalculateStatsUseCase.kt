package com.xinxe.chessle.domain.usecase

import com.xinxe.chessle.domain.model.DayRecord
import com.xinxe.chessle.domain.model.UserStats
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.lang.Exception

/**
 * 게임 종료 후 유저의 통계(스트릭, 히스토리 등)를 계산하는 유즈케이스
 */
object CalculateStatsUseCase {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun calculateNewStats(
        oldStats: UserStats,
        todayStr: String,
        usedHint: Boolean,
        attemptCount: Int
    ): UserStats {
        // 1. 날짜 데이터 준비
        val todayDate = try {
            LocalDate.parse(todayStr, DATE_FORMATTER)
        } catch (e: Exception) {
            LocalDate.now() // 파싱 실패 시 오늘 날짜 기본값
        }
        val yesterdayStr = todayDate.minusDays(1).format(DATE_FORMATTER)

        // 2. 스트릭(Streak) 업데이트
        val newStreak = calculateStreak(
            currentStreak = oldStats.currentStreak,
            lastSolvedDate = oldStats.lastSolvedDate,
            todayStr = todayStr,
            yesterdayStr = yesterdayStr
        )

        // 3. 새로운 기록(DayRecord) 생성 및 히스토리 업데이트
        val newRecord = DayRecord(
            isSolved = true,
            usedHint = usedHint,
            attemptCount = attemptCount.coerceAtLeast(1) // 최소 1회 보장
        )

        val updatedHistory = oldStats.history.toMutableMap().apply {
            put(todayStr, newRecord)
        }

        // 4. 최종 UserStats 반환
        return oldStats.copy(
            currentStreak = newStreak,
            maxStreak = maxOf(newStreak, oldStats.maxStreak),
            totalSolved = oldStats.totalSolved + 1,
            lastSolvedDate = todayStr,
            history = updatedHistory,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * 마지막 풀이 날짜를 기준으로 연속 스트릭을 계산합니다.
     */
    private fun calculateStreak(
        currentStreak: Long,
        lastSolvedDate: String,
        todayStr: String,
        yesterdayStr: String
    ): Long {
        return when (lastSolvedDate) {
            yesterdayStr -> currentStreak + 1
            todayStr -> currentStreak // 이미 오늘 풀었을 경우 (중복 호출 방지)
            else -> 1L // 어제 풀지 않았거나 기록이 없는 경우 새 시작
        }
    }
}