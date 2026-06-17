package com.xinxe.chessle.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.R
import com.xinxe.chessle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleDialog(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = ModalDeepGreen.copy(alpha = 0.76f),
        scrimColor = Color.Black.copy(alpha = 0.34f),
        tonalElevation = 0.dp,
        dragHandle = { RuleSheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.rule_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.rule_confirm_button),
                        tint = Color.White.copy(alpha = 0.58f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
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
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

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
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.rule_footer_tip),
                        // 표준 bodySmall (Bold, 12.sp)
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleSection(
    title: String,
    description: String,
    examples: List<Triple<String, String, Boolean>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            // 표준 titleMedium (Bold, 18.sp)
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
        Text(
            text = description,
            // 표준 bodyMedium (Normal, 14.sp)
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.48f)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 2.dp)) {
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
        Box(modifier = Modifier.width(92.dp), contentAlignment = Alignment.CenterStart) {
            NotationBadge(left)
        }

        Text(
            text = "vs",
            modifier = Modifier.padding(horizontal = 8.dp),
            // 표준 labelSmall 활용
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.28f)
        )

        Box(modifier = Modifier.width(112.dp), contentAlignment = Alignment.CenterStart) {
            NotationBadge(right)
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(if (isSame) R.string.rule_status_same else R.string.rule_status_diff),
            // 표준 ChessGreen(Primary)과 RuleWrong(빨강) 사용
            color = if (isSame) ChessGreen else RuleWrong,
            modifier = Modifier.width(70.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun NotationBadge(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.20f),
        shape = RoundedCornerShape(9.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            // 표준 labelSmall (Monospace 적용됨)
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun RuleSheetHandle() {
    Surface(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 10.dp)
            .size(width = 40.dp, height = 4.dp),
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.28f),
        content = {}
    )
}
