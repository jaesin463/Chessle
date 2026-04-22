package com.xinxe.chessle.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.MobileAds
import com.xinxe.chessle.data.auth.AuthManager
import com.xinxe.chessle.ui.screen.LoginScreen
import com.xinxe.chessle.ui.screen.MainScreen
import com.xinxe.chessle.ui.theme.ChesssleTheme
import com.xinxe.chessle.util.AdMobManager
import com.xinxe.chessle.viewmodel.ChessViewModel
import com.xinxe.chessle.viewmodel.ChessViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var adMobManager: AdMobManager
    private lateinit var authManager: AuthManager
    private lateinit var viewModel: ChessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 의존성 초기화 (이 안에서 adMobManager도 초기화하도록 setupDependencies에 포함)
        setupDependencies()
        setupScreenOrientation()

        splashScreen.setKeepOnScreenCondition { !viewModel.isInitialized.value }

        // AdMob 초기화 및 로드
        MobileAds.initialize(this)
        adMobManager.loadRewardedAd()

        setContent {
            ChesssleTheme {
                val isInitialized by viewModel.isInitialized.collectAsState()
                var currentDestination by remember {
                    mutableStateOf(
                        if (authManager.getCurrentUserId() == null) AppState.LOGIN
                        else AppState.MAIN
                    )
                }

                if (isInitialized) {
                    AppNavigation(
                        currentDestination = currentDestination,
                        // 2. adMobManager를 전달
                        adMobManager = adMobManager,
                        onLoginSuccess = { _ -> currentDestination = AppState.MAIN }
                    )
                }
            }
        }
    }

    /**
     * 의존성 및 뷰모델 초기화
     */
    private fun setupDependencies() {
        authManager = AuthManager(this)
        // adMobManager 초기화 추가
        adMobManager = AdMobManager(this)

        val factory = ChessViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[ChessViewModel::class.java]
    }

    /**
     * 태블릿과 모바일에 따른 화면 방향 제어
     */
    private fun setupScreenOrientation() {
        val metrics = resources.displayMetrics
        val smallestWidthDp = metrics.widthPixels / (metrics.densityDpi / 160f)

        if (smallestWidthDp < 600) {
            // 600dp 미만(일반 스마트폰)은 세로 모드로 고정
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            // 600dp 이상(태블릿)은 가로/세로 자유롭게 (또는 가로 고정)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    /**
     * 로그인과 메인 화면 간의 전환 로직 분리
     */
    @Composable
    private fun AppNavigation(
        currentDestination: AppState,
        adMobManager: AdMobManager, // 매개변수 추가
        onLoginSuccess: (String) -> Unit
    ) {
        LaunchedEffect(currentDestination) {
            if (currentDestination == AppState.MAIN) {
                viewModel.syncWithCloud()
            }
        }

        when (currentDestination) {
            AppState.LOGIN -> LoginScreen(
                authManager = authManager,
                viewModel = viewModel,
                onLoginSuccess = onLoginSuccess
            )
            AppState.MAIN -> MainScreen(
                chessViewModel = viewModel,
                adMobManager = adMobManager // MainScreen으로 전달
            )
        }
    }

    enum class AppState { LOGIN, MAIN }
}