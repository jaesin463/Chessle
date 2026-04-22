package com.xinxe.chessle.domain.repository

import com.xinxe.chessle.domain.model.MoveAttempt
import com.xinxe.chessle.domain.model.Opening
import com.xinxe.chessle.domain.model.UserStats

interface ChessRepository {
    // 오프닝 데이터 관련
    suspend fun getOpenings(): List<Opening>

    // 게임 진행 상황 관련
    fun saveGameProgress(history: List<List<MoveAttempt>>, hasWon: Boolean)
    fun loadGameProgress(): Pair<List<List<MoveAttempt>>, Boolean>?

    // 통계 관련
    fun saveUserStats(stats: UserStats)
    fun getUserStats(): UserStats

    // 히스토리 관련
    fun getPlayedIds(): Set<String>
    fun savePlayedId(id: String)
    suspend fun syncWithCloud()
}