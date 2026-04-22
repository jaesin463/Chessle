package com.xinxe.chessle.data.datasource

import android.content.Context
import com.xinxe.chessle.domain.model.MoveAttempt
import com.xinxe.chessle.domain.model.UserStats
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import androidx.core.content.edit

class LocalGameStateDataSource(context: Context) {

    // 1. 상수들은 companion object로 빼서 관리합니다 (const 사용 가능)
    companion object {
        private const val PREFS_NAME = "chessle_prefs"
        private const val KEY_SUBMITTED_HISTORY = "submitted_history"
        private const val KEY_LAST_PLAYED_DATE = "last_played_date"
        private const val KEY_HAS_WON = "has_won"
        private const val KEY_USER_STATS = "user_stats"
        private const val KEY_PLAYED_IDS = "played_opening_ids" // 추가된 히스토리 키
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")

    // 게임 진행 저장
    fun saveGameProgress(history: List<List<MoveAttempt>>, hasWon: Boolean) {
        val historyJson = json.encodeToString(history)
        val today = LocalDate.now().format(dateFormatter)
        prefs.edit().apply {
            putString(KEY_SUBMITTED_HISTORY, historyJson)
            putString(KEY_LAST_PLAYED_DATE, today)
            putBoolean(KEY_HAS_WON, hasWon)
            apply()
        }
    }

    // 게임 진행 로드
    fun loadGameProgress(): Pair<List<List<MoveAttempt>>, Boolean>? {
        val lastDate = prefs.getString(KEY_LAST_PLAYED_DATE, "")
        val today = LocalDate.now().format(dateFormatter)

        if (lastDate != today) return null

        val historyJson = prefs.getString(KEY_SUBMITTED_HISTORY, null) ?: return null
        return try {
            val history = json.decodeFromString<List<List<MoveAttempt>>>(historyJson)
            val hasWon = prefs.getBoolean(KEY_HAS_WON, false)
            Pair(history, hasWon)
        } catch (e: Exception) {
            null
        }
    }

    // 통계 저장
    fun saveUserStats(stats: UserStats) {
        val jsonString = json.encodeToString(stats)
        prefs.edit { putString(KEY_USER_STATS, jsonString) }
    }

    // 통계 로드
    fun loadUserStats(): UserStats {
        val jsonString = prefs.getString(KEY_USER_STATS, null) ?: return UserStats(lastUpdated = 0L)
        return try {
            json.decodeFromString<UserStats>(jsonString)
        } catch (e: Exception) {
            UserStats(lastUpdated = 0L)
        }
    }

    fun getPlayedIds(): Set<String> {
        return prefs.getStringSet(KEY_PLAYED_IDS, emptySet()) ?: emptySet()
    }

    fun savePlayedIds(ids: Set<String>) {
        prefs.edit { putStringSet(KEY_PLAYED_IDS, ids) }
    }
}