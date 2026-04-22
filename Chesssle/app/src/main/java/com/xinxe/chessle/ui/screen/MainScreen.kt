package com.xinxe.chessle.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinxe.chessle.R
import com.xinxe.chessle.domain.model.UserStats
import com.xinxe.chessle.ui.board.ChessBoard
import com.xinxe.chessle.ui.common.ControlButtons
import com.xinxe.chessle.ui.common.TopAppBar
import com.xinxe.chessle.ui.dialogs.HintSelectionDialog
import com.xinxe.chessle.ui.dialogs.ResultDialog
import com.xinxe.chessle.ui.dialogs.RuleDialog
import com.xinxe.chessle.ui.dialogs.StatsDialog
import com.xinxe.chessle.ui.effects.ConfettiEffect
import com.xinxe.chessle.ui.effects.RainyEffect
import com.xinxe.chessle.ui.feedback.FeedbackArea
import com.xinxe.chessle.ui.feedback.MoveInputArea
import com.xinxe.chessle.ui.theme.ChessGreen
import com.xinxe.chessle.util.AdMobManager
import com.xinxe.chessle.viewmodel.ChessUiState
import com.xinxe.chessle.viewmodel.ChessViewModel

@Composable
fun MainScreen(chessViewModel: ChessViewModel, adMobManager: AdMobManager) {
    val uiState by chessViewModel.uiState.collectAsState()
    val userStats by chessViewModel.userStats.collectAsState()

    var showResultDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showRuleDialog by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }


    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) showResultDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                streakCount = userStats.currentStreak,
                onStreakClick = { showStatsDialog = true },
                onInfoClick = { showRuleDialog = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MainContentLayout(
                uiState = uiState,
                viewModel = chessViewModel,
                onShowResult = { showResultDialog = true },
                onShowHint = { showHintDialog = true }
            )

            EffectLayer(uiState.isGameOver, uiState.hasWon, showResultDialog)

            DialogLayer(
                uiState = uiState,
                userStats = userStats,
                showStats = showStatsDialog,
                showRule = showRuleDialog,
                showResult = showResultDialog,
                showHint = showHintDialog,
                adMobManager = adMobManager,
                onDismissStats = { showStatsDialog = false },
                onDismissRule = { showRuleDialog = false },
                onDismissResult = { showResultDialog = false },
                onDismissHint = { showHintDialog = false },
                onConfirmHint = { index -> chessViewModel.applyHint(index) }
            )
        }
    }
}

@Composable
private fun MainContentLayout(
    uiState: ChessUiState,
    viewModel: ChessViewModel,
    onShowResult: () -> Unit,
    onShowHint: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val currentMaxWidth = this.maxWidth
        val currentMaxHeight = this.maxHeight
        val isTabletLandscape = currentMaxWidth >= 600.dp && currentMaxWidth > currentMaxHeight

        if (isTabletLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChessBoard(
                    modifier = Modifier
                        .weight(1.2f)
                        .aspectRatio(1f),
                    squares = uiState.squares,
                    availableMoves = uiState.availableMoves,
                    onSquareClick = { r, f -> viewModel.onSquareClick(r, f) }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    FeedbackArea(
                        submittedAttempts = uiState.submittedAttempts,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GameControlSection(uiState, viewModel, onShowResult, onShowHint)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // currentMaxWidth와 currentMaxHeight를 사용하여 하위 스코프(Column)에서의 혼동 방지
                val contentWidthModifier = if (currentMaxWidth >= 600.dp) {
                    Modifier.fillMaxWidth(0.7f)
                } else {
                    Modifier.fillMaxWidth()
                }

                ChessBoard(
                    modifier = contentWidthModifier
                        .heightIn(max = currentMaxHeight * 0.45f)
                        .aspectRatio(1f),
                    squares = uiState.squares,
                    availableMoves = uiState.availableMoves,
                    onSquareClick = { r, f -> viewModel.onSquareClick(r, f) }
                )

                FeedbackArea(
                    submittedAttempts = uiState.submittedAttempts,
                    modifier = contentWidthModifier.weight(1f)
                )

                Box(modifier = contentWidthModifier) {
                    GameControlSection(uiState, viewModel, onShowResult, onShowHint)
                }
            }
        }
    }
}

@Composable
private fun GameControlSection(
    uiState: ChessUiState,
    viewModel: ChessViewModel,
    onResultClick: () -> Unit,
    onHintClick: () -> Unit
) {
    if (!uiState.isGameOver) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MoveInputArea(uiState.currentInputMoves, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            ControlButtons(
                onUndoClick = { viewModel.undoLastMove() },
                onFillClick = { viewModel.onFillClick() },
                onHintClick = onHintClick,
                onSubmitClick = { viewModel.submitCurrentMoves() },
                isSubmitEnabled = uiState.currentMoveCount == 10,
                isFillEnabled = uiState.isFillEnabled,
                showHintButton = uiState.submittedAttempts.size >= 4,
                revealedHints = uiState.revealedHints,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Button(
            onClick = onResultClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                // 승리 시 ChessGreen, 패배 시 RuleWrong 배경 사용
                containerColor = ChessGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = stringResource(id = R.string.main_show_result),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun EffectLayer(isGameOver: Boolean, hasWon: Boolean, isDialogVisible: Boolean) {
    if (isDialogVisible && isGameOver) {
        if (hasWon) ConfettiEffect() else RainyEffect()
    }
}

@Composable
private fun DialogLayer(
    uiState: ChessUiState,
    userStats: UserStats,
    showStats: Boolean,
    showRule: Boolean,
    showResult: Boolean,
    showHint: Boolean,
    adMobManager: AdMobManager, // 매니저 추가
    onDismissStats: () -> Unit,
    onDismissRule: () -> Unit,
    onDismissResult: () -> Unit,
    onDismissHint: () -> Unit,
    onConfirmHint: (Int) -> Unit
) {
    if (showStats) StatsDialog(userStats, onDismissStats)
    if (showRule) RuleDialog(onDismissRule)
    if (showResult) ResultDialog(uiState, onDismissResult)
    if (showHint) HintSelectionDialog(adMobManager, onConfirmHint, onDismissHint)
}