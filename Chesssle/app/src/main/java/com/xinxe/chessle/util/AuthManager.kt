package com.xinxe.chessle.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    /**
     * 구글 로그인 실행 (바텀 시트 출력)
     */
    suspend fun signInWithGoogle(): FirebaseUser? {
        // 1. 구글 로그인 옵션 설정
        // R.string.default_web_client_id는 Firebase 플러그인이 자동으로 생성해줍니다.
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(com.xinxe.chessle.R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        // 2. 요청 생성
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            // 3. Credential Manager를 통해 인증 정보 획득
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            // 4. 구글 아이디 토큰인지 확인 후 Firebase 인증 진행
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()

                Log.d("AuthManager", "로그인 성공: ${authResult.user?.displayName}")
                authResult.user
            } else {
                null
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "인증 실패: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("AuthManager", "알 수 없는 오류: ${e.message}")
            null
        }
    }

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
        return try {
            // FirebaseAuth의 이메일 로그인 메서드 호출 및 suspend 확장을 위한 await() 사용
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthManager", "이메일 로그인 성공: ${authResult.user?.email}")
            authResult.user
        } catch (e: Exception) {
            Log.e("AuthManager", "이메일 로그인 실패: ${e.message}")
            null
        }
    }

    /**
     * 로그아웃
     */
    suspend fun signOut() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthManager", "로그아웃 상태 클리어 실패: ${e.message}")
        }
    }

    /**
     * 현재 로그인된 유저 ID 가져오기
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
}