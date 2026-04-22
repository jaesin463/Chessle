package com.xinxe.chessle.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.domain.model.FeedbackType
import com.xinxe.chessle.ui.theme.*
import com.xinxe.chessle.util.ImageShareUtil
import com.xinxe.chessle.viewmodel.ChessUiState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 화면에는 보이지 않지만 공유용 이미지를 렌더링하고 비트맵으로 추출하는 컴포넌트
 */
@Composable
fun InvisibleShareCanvas(
    state: ChessUiState,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()

    // 렌더링 완료 후 비트맵 추출 및 공유 실행
    LaunchedEffect(Unit) {
        delay(200) // 렌더링이 완전히 끝날 때까지 대기
        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
        ImageShareUtil.shareBitmap(context, bitmap)
        onComplete()
    }

    Box(
        modifier = Modifier
            .size(360.dp, 600.dp) // 공유 이미지 규격 최적화
            .alpha(0f) // 사용자에게는 보이지 않음
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                drawLayer(graphicsLayer)
            }
            .background(ShareBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 헤더 정보
            Text(
                text = "CHESSLE",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            )

            val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy. MM. dd"))
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 시도 횟수 결과
            Text(
                text = if (state.hasWon) "${state.submittedAttempts.size}/6" else "X/6",
                color = if (state.hasWon) GoldAccent else Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 피드백 타일 그리드
            ResultGrid(state.submittedAttempts)

            Spacer(modifier = Modifier.height(40.dp))

            // 오프닝 정보
            Text(
                text = "오늘의 오프닝",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = state.openingName,
                color = GoldAccent,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "#Chessle #체스플 #ChessPuzzle",
                color = Color.White.copy(alpha = 0.3f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ResultGrid(attempts: List<List<com.xinxe.chessle.domain.model.MoveAttempt>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        attempts.forEach { attemptRow ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                attemptRow.forEach { move ->
                    ShareTile(move.whiteFeedback)
                    ShareTile(move.blackFeedback)
                }
            }
        }
    }
}

@Composable
private fun ShareTile(feedback: FeedbackType) {
    val color = when (feedback) {
        FeedbackType.CORRECT -> ChessGreen
        FeedbackType.PRESENT -> FeedbackPresent
        FeedbackType.ABSENT -> FeedbackAbsent
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color = color, shape = RoundedCornerShape(4.dp))
    )
}