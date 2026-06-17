package com.xinxe.chessle.ui.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.domain.model.FeedbackType
import com.xinxe.chessle.domain.model.MoveAttempt
import com.xinxe.chessle.ui.theme.*

@Composable
fun FeedbackArea(
    submittedAttempts: List<List<MoveAttempt>>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(submittedAttempts) { index, attemptSet ->
                Column {
                    SubmittedRow(attemptSet)

                    if (index < submittedAttempts.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubmittedRow(attemptSet: List<MoveAttempt>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        attemptSet.forEach { attempt ->
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FeedbackSlot(attempt.whiteMove?.notation ?: "", attempt.whiteFeedback)
                FeedbackSlot(attempt.blackMove?.notation ?: "", attempt.blackFeedback)
            }
        }
    }
}

@Composable
fun RowScope.FeedbackSlot(notation: String, feedback: FeedbackType) {
    val backgroundColor = when (feedback) {
        FeedbackType.CORRECT -> FeedbackCorrect
        FeedbackType.PRESENT -> FeedbackPresent
        FeedbackType.ABSENT -> FeedbackAbsent
    }

    Surface(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f),
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
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
                color = Color.White,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
