package com.xinxe.chessle.ui.dialogs

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.xinxe.chessle.R
import com.xinxe.chessle.ui.theme.*
import com.xinxe.chessle.util.AdMobManager

@Composable
fun HintSelectionDialog(
    adMobManager: AdMobManager,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val mContext = LocalContext.current
    var selectedIndex by remember { mutableIntStateOf(-1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.88f),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(id = R.string.hint_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.hint_dialog_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(5) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            repeat(2) { col ->
                                val index = row * 2 + col
                                HintTile(
                                    index = index,
                                    isSelected = selectedIndex == index,
                                    isRevealed = false, // 다이얼로그 내에서 미리 보여줄 필요 없음
                                    solutionText = "",
                                    onClick = { selectedIndex = index },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val activity = mContext as? Activity
                    if (activity != null) {
                        adMobManager.showRewardedAd(activity) {
                            onConfirm(selectedIndex)
                            onDismiss()
                        }
                    }
                },
                enabled = selectedIndex != -1,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.hint_dialog_confirm_with_ad),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.hint_dialog_cancel),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    )
}

@Composable
private fun HintTile(
    index: Int,
    isSelected: Boolean,
    isRevealed: Boolean,
    solutionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moveNum = (index / 2) + 1
    val isWhite = index % 2 == 0

    Surface(
        onClick = onClick,
        enabled = !isRevealed,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(14.dp),
        color = when {
            isRevealed && isSelected -> GoldAccent.copy(alpha = 0.2f)
            isSelected -> GoldAccent
            // 너무 어둡지 않은, 채도가 살짝 섞인 파스텔 그레이/블루 계열
            else -> Color(0xFFDDE2E9)
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) GoldAccent else Color.Black.copy(alpha = 0.05f)
        ),
        shadowElevation = if (isSelected) 4.dp else 0.dp // 파스텔톤일 때는 미세한 그림자가 입체감을 줌
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 배경이 중간 명도(DDE2E9)이므로 순수 White 도트가 은은하게 부각됨
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        color = if (isWhite) Color.White else Color(0xFF4A4A4A),
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 번호 표시 (파스텔 톤에 어울리는 차분한 딥 그레이)
            Text(
                text = if (isWhite) "$moveNum." else "$moveNum...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected && !isRevealed) Color.Black else Color(0xFF666E7A)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // 기보 텍스트 (큼직하게 강조)
            Text(
                text = if (isRevealed && isSelected) solutionText else "",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.Black else Color(0xFF2C3E50)
            )
        }
    }
}