package com.xinxe.chessle.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class HintPreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("chessle_prefs", Context.MODE_PRIVATE)

    // 오늘 날짜 문자열 생성 (예: 20260310)
    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    // 힌트 저장 (날짜와 인덱스 함께 저장)
    fun saveTodayHint(index: Int) {
        prefs.edit().apply {
            putString("hint_date", getTodayDate())
            putInt("hint_index", index)
            apply()
        }
    }

    // 저장된 힌트 불러오기 (오늘 날짜가 아니면 -1 반환)
    fun getTodayHintIndex(): Int {
        val savedDate = prefs.getString("hint_date", "")
        return if (savedDate == getTodayDate()) {
            prefs.getInt("hint_index", -1)
        } else {
            -1 // 날짜가 지났으면 힌트 초기화
        }
    }

    fun clearTodayHint() {
        prefs.edit().apply {
            remove("hint_date")
            remove("hint_index")
            apply()
        }
    }
}
