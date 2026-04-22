package com.xinxe.chessle.ui.screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.R
import com.xinxe.chessle.data.auth.AuthManager
import com.xinxe.chessle.ui.theme.ChessGreen
import com.xinxe.chessle.viewmodel.ChessViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authManager: AuthManager,
    viewModel: ChessViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // --- 이스터 에그 및 다이얼로그 상태 추가 ---
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var showTestLoginDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // 로고 섹션에 클릭 이벤트 전달
            LogoSection(
                onLogoClick = {
                    val currentTime = System.currentTimeMillis()
                    // 2초 이내에 연속 클릭해야 카운트 유지
                    if (currentTime - lastClickTime < 2000) {
                        clickCount++
                    } else {
                        clickCount = 1
                    }
                    lastClickTime = currentTime

                    Log.d("Chessle_Debug", "clickCount: $clickCount 회")

                    if (clickCount >= 8) {
                        showTestLoginDialog = true
                        clickCount = 0
                    }
                }
            )

            Spacer(modifier = Modifier.height(80.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = ChessGreen,
                    strokeWidth = 4.dp
                )
            } else {
                GoogleSignInButton(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            val user = authManager.signInWithGoogle()
                            if (user != null) {
                                viewModel.syncWithCloud()
                                onLoginSuccess(user.uid)
                            }
                            isLoading = false
                        }
                    }
                )
            }
        }

        // 테스터 전용 로그인 다이얼로그
        if (showTestLoginDialog) {
            TestLoginDialog(
                onDismiss = { showTestLoginDialog = false },
                onLoginClick = { email, password ->
                    coroutineScope.launch {
                        isLoading = true
                        showTestLoginDialog = false
                        // AuthManager에 signInWithEmail(email, password)가 구현되어 있어야 함
                        val user = authManager.signInWithEmail(email, password)
                        if (user != null) {
                            viewModel.syncWithCloud()
                            onLoginSuccess(user.uid)
                        }
                        isLoading = false
                    }
                }
            )
        }
    }
}

@Composable
private fun LogoSection(onLogoClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 이스터 에그 클릭을 감지하기 위한 인터랙션 소스
        val interactionSource = remember { mutableStateOf(MutableInteractionSource()) }

        Surface(
            modifier = Modifier
                .size(120.dp)
                .clickable(
                    interactionSource = interactionSource.value,
                    indication = null, // 클릭 시 물결 효과(Ripple) 제거
                    onClick = onLogoClick
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Solve Daily Chess Puzzles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestLoginDialog(
    onDismiss: () -> Unit,
    onLoginClick: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tester Login") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onLoginClick(email, password) }) {
                Text("Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.85f)
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFF747775)),
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(id = R.string.login_button),
                color = Color(0xFF1F1F1F),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}