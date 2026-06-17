package com.xinxe.chessle.data.repository

import android.util.Log
import com.xinxe.chessle.data.auth.AuthManager
import com.xinxe.chessle.data.datasource.AssetOpeningDataSource
import com.xinxe.chessle.data.datasource.FirebaseDataSource
import com.xinxe.chessle.data.datasource.LocalGameStateDataSource
import com.xinxe.chessle.domain.model.*
import com.xinxe.chessle.domain.repository.ChessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChessRepositoryImpl(
    private val assetSource: AssetOpeningDataSource,
    private val localSource: LocalGameStateDataSource,
    private val firebaseSource: FirebaseDataSource,
    private val authManager: AuthManager
) : ChessRepository {

    private val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    override suspend fun getOpenings(): List<Opening> = withContext(Dispatchers.IO) {
        assetSource.fetchOpenings()
    }

    override fun saveGameProgress(history: List<List<MoveAttempt>>, hasWon: Boolean) {
        localSource.saveGameProgress(history, hasWon)
    }

    override fun loadGameProgress(): Pair<List<List<MoveAttempt>>, Boolean>? {
        return localSource.loadGameProgress()
    }

    override fun saveUserStats(stats: UserStats) {
        // 1. 로컬에 즉시 반영 (UI 반응성 우선)
        localSource.saveUserStats(stats)

        // 2. 로그인 상태라면 클라우드 백업
        authManager.getCurrentUserId()?.let { uid ->
            firebaseSource.saveStats(uid, stats)
        }
    }

    override fun getUserStats(): UserStats = localSource.loadUserStats()

    override fun getPlayedIds(): Set<String> = localSource.getPlayedIds()

    override fun savePlayedId(id: String) {
        val currentIds = localSource.getPlayedIds().toMutableSet()

        if (currentIds.add(id)) { // 새로운 ID일 경우에만 처리
            // 최대 50개 유지 (오래된 것 삭제)
            if (currentIds.size > 50) {
                val oldestId = currentIds.firstOrNull()
                oldestId?.let { currentIds.remove(it) }
            }
            localSource.savePlayedIds(currentIds)
        }
    }

    override suspend fun syncWithCloud() = withContext(Dispatchers.IO) {
        val uid = authManager.getCurrentUserId() ?: return@withContext
        val today = getTodayStr()

        try {
            val cloudStats = firebaseSource.loadStats(uid) ?: return@withContext
            val localStats = localSource.loadUserStats()

            // 1. 전체 통계 동기화 (클라우드가 더 최신이거나 성공 횟수가 더 많을 때)
            val isCloudMoreRecent = cloudStats.lastUpdated > localStats.lastUpdated
            val hasMoreCloudProgress = cloudStats.totalSolved > localStats.totalSolved

            if (isCloudMoreRecent || hasMoreCloudProgress) {
                localSource.saveUserStats(cloudStats)
                Log.d("ChessRepository", "Local stats updated from cloud.")
            }

            // 2. 오늘 풀던 기록 동기화 (오늘 이미 풀었거나 진행 중인 기록이 클라우드에 있다면)
            if (cloudStats.hasDailyProgressFor(today)) {
                localSource.saveGameProgress(
                    history = cloudStats.dailyAttempts,
                    hasWon = cloudStats.dailySolved
                )
                Log.d("ChessRepository", "Today's progress restored.")
            }
        } catch (e: Exception) {
            Log.e("ChessRepository", "Cloud sync failed", e)
        }
    }

    private fun getTodayStr(): String = dateFormatter.format(Date())

    private fun UserStats.hasDailyProgressFor(today: String): Boolean {
        val wasUpdatedToday = dateFormatter.format(Date(lastUpdated)) == today
        return lastSolvedDate == today || (wasUpdatedToday && dailyAttempts.isNotEmpty())
    }
}
