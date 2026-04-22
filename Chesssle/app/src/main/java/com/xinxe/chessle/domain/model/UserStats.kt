package com.xinxe.chessle.domain.model

import kotlinx.serialization.Serializable

/**
 * 유저의 전체 통계 및 오늘 하루의 진행 상황을 담는 모델
 */
@Serializable
data class UserStats(
    val currentStreak: Long = 0L,
    val maxStreak: Long = 0L,
    val totalSolved: Long = 0L,
    val totalAttempts: Long = 0L, // 성공 여부와 상관없는 총 제출 횟수
    val lastSolvedDate: String = "", // "yyyyMMdd" 형식

    // 날짜별 성공 기록 (Key: "yyyyMMdd")
    val history: Map<String, DayRecord> = emptyMap(),

    // --- 오늘 하루 세션 정보 (매일 초기화 대상) ---
    val dailyAttempts: List<List<MoveAttempt>> = emptyList(),
    val dailySolved: Boolean = false,
    val dailyUsedHint: Boolean = false,
    val revealedHintIndex: Int = -1,

    // 데이터 동기화 및 최신성 판별용
    val lastUpdated: Long = System.currentTimeMillis(),
) {
    /**
     * 전체 성공률 (%) 계산
     */
    val winRate: Int
        get() = if (totalAttempts > 0) ((totalSolved.toDouble() / totalAttempts) * 100).toInt() else 0

    /**
     * 특정 날짜의 성공 기록 여부 확인
     */
    fun isSolvedAt(dateStr: String): Boolean = history[dateStr]?.isSolved ?: false

    /**
     * 특정 날짜의 시도 횟수 가져오기 (달력 UI 농도 조절용)
     */
    fun getAttemptCountAt(dateStr: String): Int = history[dateStr]?.attemptCount ?: 0
}

/**
 * 특정 날짜의 풀이 결과 상세 정보
 */
@Serializable
data class DayRecord(
    val isSolved: Boolean,
    val usedHint: Boolean,
    val attemptCount: Int, // 몇 번째 시도에서 성공했는지
    val solvedAt: Long = System.currentTimeMillis() // 풀이 완료 타임스탬프
)