package com.xinxe.chessle.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.R
import com.xinxe.chessle.domain.model.BoardFactory
import com.xinxe.chessle.domain.model.FeedbackType
import com.xinxe.chessle.domain.model.Square
import com.xinxe.chessle.domain.usecase.MovePieceUseCase
import com.xinxe.chessle.ui.board.ChessBoard
import com.xinxe.chessle.ui.theme.*
import com.xinxe.chessle.util.NotationFormatter
import com.xinxe.chessle.viewmodel.ChessUiState

@Composable
fun ResultDialog(
    state: ChessUiState,
    onDismiss: () -> Unit,
) {
    var isSharing by remember { mutableStateOf(false) }
    var currentStep by remember(state.solution) { mutableIntStateOf(state.solution.size) }

    val displayedSquares = remember(state.solution, currentStep) {
        var squares = BoardFactory.createInitialBoard()
        state.solution.take(currentStep).forEachIndexed { index, notation ->
            NotationFormatter.parse(notation, squares, index % 2 == 0)?.let { (from, to) ->
                val (nextSquares, _) = MovePieceUseCase.execute(from, to, squares)
                squares = nextSquares
            }
        }
        squares
    }

    val stepText = remember(currentStep, state.solution) {
        if (currentStep == 0) "..."
        else {
            val idx = currentStep - 1
            val num = (idx / 2) + 1
            if (idx % 2 == 0) "$num. ${state.solution[idx]}" else "$num... ${state.solution[idx]}"
        }
    }

    Box {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = { },
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.background,
            title = { ResultDialogTitle(state.hasWon, onDismiss) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BoardSection(displayedSquares, stepText, currentStep, state.solution.size) {
                        currentStep = it
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AttemptGrid(state.submittedAttempts)

                    Spacer(modifier = Modifier.height(16.dp))

                    OpeningInfo(state.openingName, state.solution)

                    Spacer(modifier = Modifier.height(16.dp))

                    ShareButton(isSharing) { isSharing = true }
                }
            }
        )

        if (isSharing) {
            InvisibleShareCanvas(state = state, onComplete = { isSharing = false })
        }
    }
}

@Composable
private fun ResultDialogTitle(hasWon: Boolean, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(if (hasWon) R.string.result_success else R.string.result_fail),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BoardSection(
    squares: List<Square>,
    stepText: String,
    currentStep: Int,
    totalSteps: Int,
    onStepChange: (Int) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(1f)) {
            ChessBoard(
                squares = squares,
                availableMoves = emptyList(),
                onSquareClick = { _, _ -> },
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            val arrowColor =
                if (currentStep > 0) textColor else MaterialTheme.colorScheme.onSurfaceVariant
            IconButton(onClick = { onStepChange(currentStep - 1) }, enabled = currentStep > 0) {
                Text("<", color = arrowColor, style = MaterialTheme.typography.headlineSmall)
            }
            Text(
                text = stepText,
                modifier = Modifier.width(100.dp),
                textAlign = TextAlign.Center,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            val nextArrowColor =
                if (currentStep < totalSteps) textColor else MaterialTheme.colorScheme.onSurfaceVariant
            IconButton(
                onClick = { onStepChange(currentStep + 1) },
                enabled = currentStep < totalSteps
            ) {
                Text(">", color = nextArrowColor, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun AttemptGrid(attempts: List<List<com.xinxe.chessle.domain.model.MoveAttempt>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        attempts.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { attempt ->
                    MiniTile(attempt.whiteFeedback, Modifier.weight(1f))
                    MiniTile(attempt.blackFeedback, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OpeningInfo(name: String, solution: List<String>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            color = GoldAccent,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = NotationFormatter.format(solution),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ShareButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ChessGreen,
            contentColor = Color.White
        ),
        enabled = !isLoading,
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
        } else {
            Text(
                text = stringResource(R.string.result_share),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun MiniTile(feedback: FeedbackType, modifier: Modifier = Modifier) {
    val color = when (feedback) {
        FeedbackType.CORRECT -> FeedbackCorrect
        FeedbackType.PRESENT -> FeedbackPresent
        FeedbackType.ABSENT -> FeedbackAbsent
    }

    Surface(
        modifier = modifier.aspectRatio(1f),
        color = color,
        shape = RoundedCornerShape(2.dp)
    ) {}
}