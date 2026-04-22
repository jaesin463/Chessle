package com.xinxe.chessle.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xinxe.chessle.data.auth.AuthManager
import com.xinxe.chessle.data.datasource.AssetOpeningDataSource
import com.xinxe.chessle.data.datasource.FirebaseDataSource
import com.xinxe.chessle.data.datasource.LocalGameStateDataSource
import com.xinxe.chessle.data.repository.ChessRepositoryImpl
import com.xinxe.chessle.util.HintPreferenceManager

class ChessViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChessViewModel::class.java)) {
            // 1. 필요한 DataSource들을 생성합니다.
            val assetDataSource = AssetOpeningDataSource(context)
            val localDataSource = LocalGameStateDataSource(context)
            val firebaseDataSource = FirebaseDataSource()
            val authManager = AuthManager(context)

            val hintPrefs = HintPreferenceManager(context)

            // 2. DataSource들을 합쳐서 Repository 구현체를 만듭니다.
            val repository = ChessRepositoryImpl(assetDataSource, localDataSource, firebaseDataSource, authManager)

            // 3. 완성된 Repository를 ViewModel에 주입합니다.
            @Suppress("UNCHECKED_CAST")
            return ChessViewModel(hintPrefs, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}