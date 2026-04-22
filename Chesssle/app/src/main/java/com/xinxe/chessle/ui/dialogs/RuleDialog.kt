package com.xinxe.chessle.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.R
import com.xinxe.chessle.ui.theme.*

@Composable
fun RuleDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.rule_confirm_button),
                    // 표준 labelLarge (Bold, 15.sp)
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        // 표준 SurfaceGrey 사용 (MaterialTheme 설정에 따름)
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(id = R.string.rule_dialog_title),
                // 표준 headlineSmall (Bold, 20.sp, 자간 1.sp)
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 섹션 1: 기물 이동 및 특수 기호 규칙
                RuleSection(
                    title = stringResource(id = R.string.rule_section_1_title),
                    description = stringResource(id = R.string.rule_section_1_desc),
                    examples = listOf(
                        Triple("Nxe5", "Ne5", false),
                        Triple("Qh5+", "Qh5", false),
                        Triple("e5", "e5", true)
                    )
                )

                // 표준 구분선 (Color.kt에서 정의한 RuleDivider 활용 가능)
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                // 섹션 2: 색상 및 좌표 규칙
                val white = stringResource(id = R.string.piece_white)
                val black = stringResource(id = R.string.piece_black)

                RuleSection(
                    title = stringResource(id = R.string.rule_section_2_title),
                    description = stringResource(id = R.string.rule_section_2_desc),
                    examples = listOf(
                        Triple("Nxe5", "Ne5", true),
                        Triple("Qh5+", "Qh5", true),
                        Triple("e5 ($white)", "e5 ($black)", false)
                    )
                )

                // 푸터 팁 (표준 스타일 적용)
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.rule_footer_tip),
                        // 표준 bodySmall (Bold, 12.sp)
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun RuleSection(
    title: String,
    description: String,
    examples: List<Triple<String, String, Boolean>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            // 표준 titleMedium (Bold, 18.sp)
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            // 표준 bodyMedium (Normal, 14.sp)
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            examples.forEach { (left, right, isSame) ->
                ComparisonRow(left, right, isSame)
            }
        }
    }
}

@Composable
private fun ComparisonRow(left: String, right: String, isSame: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            NotationBadge(left)
        }

        Text(
            text = "vs",
            modifier = Modifier.padding(horizontal = 8.dp),
            // 표준 labelSmall 활용
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            NotationBadge(right)
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(if (isSame) R.string.rule_status_same else R.string.rule_status_diff),
            // 표준 ChessGreen(Primary)과 RuleWrong(빨강) 사용
            color = if (isSame) ChessGreen else RuleWrong,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun NotationBadge(text: String) {
    Surface(
        color = NotationBadgeBg, // 표준: 0xFF424242
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            // 표준 labelSmall (Monospace 적용됨)
            style = MaterialTheme.typography.labelSmall
        )
    }
}