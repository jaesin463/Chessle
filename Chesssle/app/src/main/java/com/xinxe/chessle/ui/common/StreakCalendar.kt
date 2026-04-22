package com.xinxe.chessle.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.domain.model.DayRecord
import com.xinxe.chessle.domain.model.UserStats
import com.xinxe.chessle.ui.theme.ChessGreen
import com.xinxe.chessle.ui.theme.GoldAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("DefaultLocale")
@Composable
fun StreakCalendar(
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    // 1. 현재 달력을 보여줄 기준 날짜 상태 (기본값: 오늘)
    var viewDate by remember { mutableStateOf(LocalDate.now()) }

    val firstDayOfMonth = viewDate.withDayOfMonth(1)
    val daysInMonth = viewDate.lengthOfMonth()
    // 요일 시작 위치 (Monday=1, ..., Sunday=7) -> 일요일을 0으로 맞추기 위해 조정
    val startDayOffset = (firstDayOfMonth.dayOfWeek.value % 7)

    Column(modifier = modifier.fillMaxWidth()) {
        // --- 헤더: 월 이동 컨트롤 ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewDate = viewDate.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
            }

            Text(
                text = viewDate.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = { viewDate = viewDate.plusMonths(1) },
                // 미래는 볼 수 없도록 제한 (선택 사항)
                enabled = viewDate.withDayOfMonth(1).isBefore(LocalDate.now().withDayOfMonth(1))
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }

        // --- 요일 표시 (S M T W T F S) ---
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 날짜 그리드 (6행 7열) ---
        var currentDay = 1
        for (row in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIdx = row * 7 + col
                    if (cellIdx < startDayOffset || currentDay > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val dateKey = String.format("%04d%02d%02d", viewDate.year, viewDate.monthValue, currentDay)
                        val record = stats.history[dateKey]
                        DayCell(
                            day = currentDay,
                            status = getDayStatusFromHistory(dateKey, stats.history),
                            attemptCount = record?.attemptCount ?: 0,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                // TODO: 나중에 복기 기능을 위해 클릭 이벤트 처리 가능
                            }
                        )
                        currentDay++
                    }
                }
            }
            // 모든 날짜를 다 그렸으면 다음 행을 만들지 않음
            if (currentDay > daysInMonth) break
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    status: DayStatus,
    attemptCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val intensity = when {
        status == DayStatus.NONE -> 1.0f
        else -> (1.1f - (attemptCount.toFloat() / 12f)).coerceIn(0.5f, 1.0f)
    }

    val backgroundColor = when (status) {
        DayStatus.PERFECT -> ChessGreen.copy(alpha = intensity)
        DayStatus.HINTED -> GoldAccent
        DayStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        DayStatus.NONE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .clickable(enabled = status != DayStatus.NONE) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (status == DayStatus.NONE) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                Color.Black
            }
        )
    }
}

/**
 * UserStats의 history 데이터를 바탕으로 해당 날짜의 상태를 반환합니다.
 */
private fun getDayStatusFromHistory(dateKey: String, history: Map<String, DayRecord>): DayStatus {
    val record = history[dateKey] ?: return DayStatus.NONE
    return if (record.isSolved) {
        if (record.usedHint) DayStatus.HINTED else DayStatus.PERFECT
    } else {
        DayStatus.FAILED
    }
}

enum class DayStatus { NONE, PERFECT, HINTED, FAILED }