package com.xinxe.chessle.ui.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.domain.model.MoveAttempt
import com.xinxe.chessle.ui.theme.*

@Composable
fun MoveInputArea(
    inputMoves: List<MoveAttempt>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            inputMoves.forEach { attempt ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 백/흑 입력 슬롯
                    MoveSlot(attempt.whiteMove?.notation ?: "", isWhite = true)
                    MoveSlot(attempt.blackMove?.notation ?: "", isWhite = false)
                }
            }
        }
    }
}

@Composable
fun RowScope.MoveSlot(notation: String, isWhite: Boolean) {
    val slotBgColor = if (isWhite) BoardLight else DarkGrey
    val textColor = if (isWhite) DarkGrey else Color.White

    Surface(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f),
        shape = RoundedCornerShape(4.dp),
        color = slotBgColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val fontSize = with(LocalDensity.current) { (maxWidth.toPx() * 0.35f).toSp() }

            Text(
                text = notation,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black
                ),
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
