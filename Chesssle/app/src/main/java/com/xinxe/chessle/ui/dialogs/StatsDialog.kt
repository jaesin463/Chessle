package com.xinxe.chessle.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.R
import com.xinxe.chessle.domain.model.UserStats
import com.xinxe.chessle.ui.common.StreakCalendar
import com.xinxe.chessle.ui.theme.GoldAccent
import com.xinxe.chessle.ui.theme.ModalDeepGreen

/**
 * 유저의 풀이 통계와 스트릭 달력을 보여주는 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDialog(
    stats: UserStats,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = ModalDeepGreen.copy(alpha = 0.76f),
        scrimColor = Color.Black.copy(alpha = 0.34f),
        tonalElevation = 0.dp,
        dragHandle = { SheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.stats_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f),
                    color = Color.White
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.stats_close),
                        tint = Color.White.copy(alpha = 0.58f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 핵심 지표 영역 (가로 나열)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    StatBox(
                        label = stringResource(id = R.string.stats_total_solved),
                        value = stats.totalSolved.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    // 현재 스트릭 강조
                    StatBox(
                        label = stringResource(id = R.string.stats_current_streak),
                        value = stats.currentStreak.toString(),
                        isHighlight = true,
                        modifier = Modifier.weight(1f)
                    )

                    StatBox(
                        label = stringResource(id = R.string.stats_best_streak),
                        value = stats.maxStreak.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 18.dp),
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.08f)
                )

                Text(
                    text = "활동 기록",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Start
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    StreakCalendar(stats = stats)
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = if (isHighlight) GoldAccent else Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.42f),
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SheetHandle() {
    Surface(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 10.dp)
            .size(width = 40.dp, height = 4.dp),
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.28f),
        content = {}
    )
}
