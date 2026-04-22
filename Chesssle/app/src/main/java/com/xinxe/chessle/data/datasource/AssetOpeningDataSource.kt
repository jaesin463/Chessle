package com.xinxe.chessle.data.datasource

import android.content.Context
import com.xinxe.chessle.domain.model.Opening
import kotlinx.serialization.json.Json

class AssetOpeningDataSource(private val context: Context) {
    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun fetchOpenings(): List<Opening> {
        return try {
            // assets 폴더의 openings.json을 읽어옴
            val jsonString = context.assets.open("openings.json")
                .bufferedReader()
                .use { it.readText() }

            // JSON 문자열을 Opening 객체 리스트로 변환
            jsonParser.decodeFromString<List<Opening>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}