package com.xinxe.chessle.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.R
import com.xinxe.chessle.domain.model.UserStats
import com.xinxe.chessle.ui.common.StreakCalendar
import com.xinxe.chessle.ui.theme.GoldAccent

/**
 * 유저의 풀이 통계와 스트릭 달력을 보여주는 다이얼로그
 */
@Composable
fun StatsDialog(
    stats: UserStats,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.stats_close),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(id = R.string.stats_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 핵심 지표 영역 (가로 나열)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    StatBox(
                        label = stringResource(id = R.string.stats_total_solved),
                        value = stats.totalSolved.toString()
                    )

                    // 현재 스트릭 강조
                    StatBox(
                        label = stringResource(id = R.string.stats_current_streak),
                        value = stats.currentStreak.toString(),
                        isHighlight = true
                    )

                    StatBox(
                        label = stringResource(id = R.string.stats_best_streak),
                        value = stats.maxStreak.toString()
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 20.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // 2. 스트릭 달력 영역
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
                        .wrapContentHeight()
                ) {
                    StreakCalendar(stats = stats)
                }
            }
        }
    )
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.displayMedium
            else MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = if (isHighlight) GoldAccent else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}