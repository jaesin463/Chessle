package com.xinxe.chessle.domain.usecase

import android.util.Log
import com.xinxe.chessle.domain.model.Opening
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

object GetDailyQuestionUseCase {
    // 앱 출시일 (전 세계 유저 공통 기준점)
    private val BASE_DATE = LocalDate.of(2026, 1, 1)

    private const val PENALTY_FACTOR = 0.2
    private const val RECOVERY_DAYS = 5
    private const val NAME_HISTORY_SIZE = 7

    fun getTodayQuestion(allOpenings: List<Opening>): Opening? {
        if (allOpenings.isEmpty()) return null

        val today = LocalDate.now()
        val daysSinceStart = ChronoUnit.DAYS.between(BASE_DATE, today)

        // 만약 오늘이 출시일보다 전이라면 (시스템 시간 오류 등)
        if (daysSinceStart < 0) return allOpenings.first()

        // 1. [핵심] 출시일부터 오늘까지의 '가중치 타임라인'을 순차적으로 시뮬레이션
        // 이 리스트는 전 세계 모든 유저가 '오늘이 며칠째냐'에 따라 항상 동일한 순서로 계산됨
        val historyCodes = mutableListOf<Char>()
        val historyRoots = mutableListOf<String>()

        var todaySelection: Opening? = null

        // 출시 1일차부터 오늘까지 루프를 돌며 "정답지"를 생성함 (메모리상에서 순식간에 진행)
        for (day in 0..daysSinceStart) {
            val dateForDay = BASE_DATE.plusDays(day)
            val seed = dateForDay.toEpochDay()

            // 현재 시점의 가중치 계산 (최근 기록 기준)
            val currentWeights = calculateWeightsFromHistory(historyCodes.takeLast(RECOVERY_DAYS))
            val currentRecentRoots = historyRoots.takeLast(NAME_HISTORY_SIZE).toSet()

            // 해당 날짜의 문제를 결정론적으로 선택
            val selected = selectQuestionForDay(allOpenings, seed, currentWeights, currentRecentRoots)

            // 역사(History)에 기록
            historyCodes.add(selected.code.first())
            historyRoots.add(extractRootName(selected.openingName))

            // 오늘 날짜라면 결과값으로 저장
            if (day == daysSinceStart) {
                todaySelection = selected
            }
        }

        Log.d("Chessle_Debug", "$todaySelection")

        return todaySelection
    }

    private fun selectQuestionForDay(
        allOpenings: List<Opening>,
        seed: Long,
        weights: Map<Char, Double>,
        recentRoots: Set<String>
    ): Opening {
        var result: Opening? = null
        for (i in 0 until 10) { // 이름 중복 피하기 위해 최대 10번 시도
            val random = Random(seed + i)
            val group = selectGroupByWeight(weights, random)
            val candidates = allOpenings.filter { it.code.first() == group }

            if (candidates.isEmpty()) continue

            val candidate = candidates[random.nextInt(candidates.size)]
            if (!recentRoots.contains(extractRootName(candidate.openingName))) {
                result = candidate
                break
            }
            result = candidate
        }
        return result ?: allOpenings[Random(seed).nextInt(allOpenings.size)]
    }

    private fun calculateWeightsFromHistory(recentCodes: List<Char>): Map<Char, Double> {
        val weights = mutableMapOf('A' to 1.0, 'B' to 1.0, 'C' to 1.0, 'D' to 1.0, 'E' to 1.0)
        recentCodes.reversed().forEachIndexed { index, codeGroup ->
            val dayDistance = index + 1
            val penalty = PENALTY_FACTOR + (1.0 - PENALTY_FACTOR) * (dayDistance.toDouble() / RECOVERY_DAYS)
            weights[codeGroup] = minOf(weights[codeGroup] ?: 1.0, penalty)
        }
        return weights
    }

    private fun selectGroupByWeight(weights: Map<Char, Double>, random: Random): Char {
        val total = weights.values.sum()
        var r = random.nextDouble() * total
        for ((group, weight) in weights) {
            r -= weight
            if (r <= 0) return group
        }
        return 'A'
    }

    private fun extractRootName(fullName: String): String = fullName.split(":", ",").first().trim()
}