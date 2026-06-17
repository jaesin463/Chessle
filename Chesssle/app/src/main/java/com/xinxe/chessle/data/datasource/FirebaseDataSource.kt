package com.xinxe.chessle.data.datasource

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xinxe.chessle.BuildConfig
import com.xinxe.chessle.domain.model.UserStats
import kotlinx.coroutines.tasks.await

class FirebaseDataSource {
    private val database = Firebase.database.reference.child("users")

    fun saveStats(userId: String, stats: UserStats) {
        database.child(userId).child("stats").setValue(stats)
            .addOnSuccessListener { if (BuildConfig.DEBUG) Log.d("Firebase", "데이터 저장 성공") }
            .addOnFailureListener { e -> Log.e("Firebase", "저장 실패", e) }
    }

    suspend fun loadStats(userId: String): UserStats? {
        return try {
            val snapshot = database.child(userId).child("stats").get().await()
            val stats = snapshot.getValue(UserStats::class.java)
            if (BuildConfig.DEBUG) Log.d("Firebase", "데이터 로드 성공")
            stats
        } catch (e: Exception) {
            Log.e("Firebase", "데이터 로드 실패", e)
            null
        }
    }
}
