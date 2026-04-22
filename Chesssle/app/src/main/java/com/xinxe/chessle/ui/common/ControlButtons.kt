package com.xinxe.chessle.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.R
import com.xinxe.chessle.ui.theme.ChessGreen
import com.xinxe.chessle.ui.theme.GoldAccent

@Composable
fun ControlButtons(
    onUndoClick: () -> Unit,
    onFillClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onHintClick: () -> Unit,
    isSubmitEnabled: Boolean,
    isFillEnabled: Boolean,
    showHintButton: Boolean,
    revealedHints: Map<Int, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(10.dp)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Undo (존재감 있는 딥 로즈)
        ResponsiveButton(
            onClick = onUndoClick,
            modifier = Modifier.weight(1f),
            shape = buttonShape,
            containerColor = Color(0xFF8C3E44),
            contentColor = Color.White,
            text = "Undo"
        )

        // 2. Fill (존재감 있는 스틸 블루)
        ResponsiveButton(
            onClick = onFillClick,
            enabled = isFillEnabled,
            modifier = Modifier.weight(1f),
            shape = buttonShape,
            containerColor = Color(0xFF3E5E8C),
            contentColor = Color.White,
            text = "Fill"
        )

        // 3. Hint (황금색 유지)
        if (showHintButton) {
            val usedHintKey = revealedHints.keys.firstOrNull()
            val hintNotation = revealedHints[usedHintKey]

            val isHintRevealed = hintNotation != null
            val displayHintText = if (usedHintKey != null && hintNotation != null) {
                val moveNum = (usedHintKey / 2) + 1
                val isWhite = usedHintKey % 2 == 0

                if (isWhite) {
                    stringResource(id = R.string.hint_format_white, moveNum, hintNotation)
                } else {
                    stringResource(id = R.string.hint_format_black, moveNum, hintNotation)
                }
            } else {
                stringResource(id = R.string.hint_default_label)
            }

            ResponsiveButton(
                onClick = onHintClick,
                enabled = !isHintRevealed,
                modifier = Modifier.weight(1f),
                shape = buttonShape,
                containerColor = if (!isHintRevealed) {
                    GoldAccent
                } else {
                    GoldAccent.copy(alpha = 0.7f)
                },
                contentColor = Color.Black,
                text = displayHintText,
                isBold = true
            )
        }

        // 4. Submit (메인 초록)
        ResponsiveButton(
            onClick = onSubmitClick,
            enabled = isSubmitEnabled,
            modifier = Modifier.weight(1f),
            shape = buttonShape,
            containerColor = ChessGreen,
            contentColor = Color.White,
            disabledContainerColor = ChessGreen.copy(alpha = 0.2f),
            text = "Submit",
            isBold = true
        )
    }
}

@Composable
fun ResponsiveButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isBold: Boolean = false,
    shape: CornerBasedShape = RoundedCornerShape(8.dp),
    containerColor: Color,
    contentColor: Color,
    disabledContainerColor: Color = containerColor.copy(alpha = 0.5f)
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = shape,
        contentPadding = PaddingValues(horizontal = 2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold
        )
    }
}