package com.xinxe.chessle.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.xinxe.chessle.domain.model.*
import com.xinxe.chessle.domain.repository.ChessRepository
import com.xinxe.chessle.domain.usecase.*
import com.xinxe.chessle.util.HintPreferenceManager
import com.xinxe.chessle.util.NotationFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChessViewModel(
    private val hintPrefs: HintPreferenceManager,
    private val repository: ChessRepository
) : ViewModel() {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    private var solutionNotations = listOf<String>()
    private val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        initialSetup()
    }

    private fun initialSetup() {
        viewModelScope.launch {
            try {
                // 1. 데이터 로드 (Repository 인터페이스의 중첩 리스트 구조 반영)
                val allData = withContext(Dispatchers.IO) { repository.getOpenings() }
                val progress: Pair<List<List<MoveAttempt>>, Boolean>? =
                    repository.loadGameProgress()
                val localStats = repository.getUserStats()

                _userStats.value = localStats
                val today = getTodayStr()

                GetDailyQuestionUseCase.getTodayQuestion(allData)?.let { opening ->
                    solutionNotations = opening.solution

                    // history의 타입을 List<List<MoveAttempt>>로 명시하여 에러 방지
                    val history: List<List<MoveAttempt>> = progress?.first ?: emptyList()
                    val won: Boolean = progress?.second ?: false

                    val isTodaySolved = localStats.lastSolvedDate == today

                    val savedHintIndex = hintPrefs.getTodayHintIndex()

                    _uiState.updateWithFillCheck { state ->
                        state.copy(
                            openingName = opening.openingName,
                            solution = opening.solution,
                            solutionText = NotationFormatter.format(opening.solution),
                            squares = BoardFactory.createInitialBoard(),
                            submittedAttempts = history,
                            hasWon = won,
                            isGameOver = won || history.size >= 6,
                            dailyUsedHint = if (isTodaySolved) localStats.dailyUsedHint else false,
                            revealedHints = if (savedHintIndex != -1) {
                                mapOf(savedHintIndex to opening.solution[savedHintIndex])
                            } else {
                                emptyMap()
                            }
                        )
                    }
                }
                syncWithCloud()
            } catch (e: Exception) {
                Log.e("ChessViewModel", "Initialization Failed", e)
            } finally {
                _isInitialized.value = true
            }
        }
    }

    suspend fun syncWithCloud() {
        repository.syncWithCloud()
        val updatedStats = repository.getUserStats()
        _userStats.update { updatedStats }

        val today = getTodayStr()
        if (updatedStats.hasDailyProgressFor(today)) {
            _uiState.updateWithFillCheck {
                it.copy(
                    submittedAttempts = updatedStats.dailyAttempts, // List<List<MoveAttempt>>
                    hasWon = updatedStats.dailySolved,
                    isGameOver = updatedStats.dailySolved || updatedStats.dailyAttempts.size >= 6
                )
            }
        }
    }

    fun applyHint(index: Int) {
        val hintText = solutionNotations.getOrNull(index) ?: return

        // 1. UI 상태 업데이트
        _uiState.update {
            it.copy(
                revealedHints = mapOf(index to hintText),
                dailyUsedHint = true
            )
        }

        // 2. 로컬 DB(UserStats) 업데이트
        val updatedStats = _userStats.value.copy(
            dailyUsedHint = true,
            revealedHintIndex = index
        )
        _userStats.value = updatedStats

        // 3. SharedPreferences에 영구 저장 (앱 재실행 대비)
        hintPrefs.saveTodayHint(index)

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserStats(updatedStats)
        }
    }

    fun onSquareClick(rank: Int, file: Int) {
        val state = _uiState.value
        if (state.isGameOver) return

        val clickedSquare = state.squares.find { it.rank == rank && it.file == file } ?: return

        if (state.selectedSquare != null && state.availableMoves.contains(Position(rank, file))) {
            val (newSquares, move) = MovePieceUseCase.execute(
                state.selectedSquare,
                clickedSquare,
                state.squares
            )
            applyMoveResult(newSquares, move)
            return
        }

        if (clickedSquare.piece?.color == state.currentTurn) {
            val moves = ValidateMoveUseCase.getValidMoves(clickedSquare, state.squares)
            _uiState.update { it.copy(selectedSquare = clickedSquare, availableMoves = moves) }
        } else {
            _uiState.update { it.copy(selectedSquare = null, availableMoves = emptyList()) }
        }
    }

    private fun applyMoveResult(newSquares: List<Square>, move: Move) {
        _uiState.updateWithFillCheck { state ->
            if (state.currentMoveCount >= 10) return@updateWithFillCheck state

            val newList = state.currentInputMoves.toMutableList()
            val moveIdx = state.currentMoveCount / 2

            newList[moveIdx] = if (state.currentMoveCount % 2 == 0) {
                newList[moveIdx].copy(whiteMove = move)
            } else {
                newList[moveIdx].copy(blackMove = move)
            }

            state.copy(
                squares = newSquares,
                currentInputMoves = newList,
                currentMoveCount = state.currentMoveCount + 1,
                currentTurn = if (state.currentMoveCount % 2 == 0) PieceColor.BLACK else PieceColor.WHITE,
                selectedSquare = null,
                availableMoves = emptyList()
            )
        }
    }

    fun submitCurrentMoves() {
        _uiState.updateWithFillCheck { state ->
            if (state.currentMoveCount < 10 || state.hasWon) return@updateWithFillCheck state

            val (newAttempt, won) = ProcessFeedbackUseCase.execute(
                state.currentInputMoves,
                solutionNotations
            )

            // newHistory는 List<List<MoveAttempt>>가 됩니다.
            val newHistory = state.submittedAttempts + listOf(newAttempt)
            val today = getTodayStr()
            val currentStats = _userStats.value

            val updatedStats = if (won) {
                CalculateStatsUseCase.calculateNewStats(
                    currentStats,
                    today,
                    state.dailyUsedHint,
                    newHistory.size
                )
                    .copy(
                        dailyAttempts = newHistory,
                        dailySolved = true,
                        totalAttempts = currentStats.totalAttempts + 1
                    )
            } else {
                currentStats.copy(
                    dailyAttempts = newHistory,
                    dailySolved = false,
                    totalAttempts = currentStats.totalAttempts + 1,
                    lastUpdated = System.currentTimeMillis()
                )
            }

            _userStats.value = updatedStats
            saveStatsToInternal(updatedStats, newHistory, won)
            logSubmitEvent(state.openingName, won, newHistory.size)

            state.copy(
                squares = BoardFactory.createInitialBoard(),
                currentInputMoves = List(5) { MoveAttempt() },
                currentMoveCount = 0,
                submittedAttempts = newHistory,
                isGameOver = won || newHistory.size >= 6,
                hasWon = won
            )
        }
    }

    fun undoLastMove() {
        _uiState.updateWithFillCheck { state ->
            if (state.currentMoveCount <= 0) return@updateWithFillCheck state

            val moveIndex = state.currentMoveCount - 1
            val attemptIndex = moveIndex / 2
            val isWhiteUndo = moveIndex % 2 == 0

            val currentAttempt = state.currentInputMoves.getOrNull(attemptIndex)
            val lastMove = if (isWhiteUndo) currentAttempt?.whiteMove else currentAttempt?.blackMove

            lastMove ?: return@updateWithFillCheck state
            val restoredSquares = UndoMoveUseCase.execute(state.squares, lastMove)

            val newList = state.currentInputMoves.toMutableList()
            newList[attemptIndex] = if (isWhiteUndo) newList[attemptIndex].copy(whiteMove = null)
            else newList[attemptIndex].copy(blackMove = null)

            state.copy(
                squares = restoredSquares,
                currentInputMoves = newList,
                currentMoveCount = moveIndex,
                currentTurn = if (isWhiteUndo) PieceColor.WHITE else PieceColor.BLACK
            )
        }
    }

    fun onFillClick() {
        _uiState.updateWithFillCheck { state ->
            val lastAttempt =
                state.submittedAttempts.lastOrNull() ?: return@updateWithFillCheck state
            val lastCorrectPath = lastAttempt.flatMap {
                listOf(
                    it.whiteMove to it.whiteFeedback,
                    it.blackMove to it.blackFeedback
                )
            }

            val movesToFill = mutableListOf<Move>()
            for (i in state.currentMoveCount until lastCorrectPath.size) {
                val (move, feedback) = lastCorrectPath[i]
                if (move != null && feedback == FeedbackType.CORRECT) movesToFill.add(move) else break
            }

            if (movesToFill.isEmpty()) return@updateWithFillCheck state

            var currentSquares = state.squares
            val newList = state.currentInputMoves.toMutableList()

            movesToFill.forEachIndexed { index, move ->
                val actualIndex = state.currentMoveCount + index
                val fromSq =
                    currentSquares.find { it.rank == move.from.rank && it.file == move.from.file }
                val toSq =
                    currentSquares.find { it.rank == move.to.rank && it.file == move.to.file }

                if (fromSq != null && toSq != null) {
                    val (updatedSquares, _) = MovePieceUseCase.execute(fromSq, toSq, currentSquares)
                    currentSquares = updatedSquares
                    val attemptIdx = actualIndex / 2
                    newList[attemptIdx] =
                        if (actualIndex % 2 == 0) newList[attemptIdx].copy(whiteMove = move)
                        else newList[attemptIdx].copy(blackMove = move)
                }
            }

            val totalMoves = state.currentMoveCount + movesToFill.size
            state.copy(
                squares = currentSquares,
                currentInputMoves = newList,
                currentMoveCount = totalMoves,
                currentTurn = if (totalMoves % 2 == 0) PieceColor.WHITE else PieceColor.BLACK
            )
        }
    }
    // --- Helpers ---

    private fun getTodayStr(): String = dateFormatter.format(Date())

    private fun UserStats.hasDailyProgressFor(today: String): Boolean {
        val wasUpdatedToday = dateFormatter.format(Date(lastUpdated)) == today
        return lastSolvedDate == today || (wasUpdatedToday && dailyAttempts.isNotEmpty())
    }

    private fun saveStatsToInternal(
        stats: UserStats,
        history: List<List<MoveAttempt>>,
        won: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserStats(stats)
            repository.saveGameProgress(history, won) // Repository 시그니처와 일치
        }
    }

    private fun checkFillEnabled(state: ChessUiState): Boolean {
        val lastAttempt = state.submittedAttempts.lastOrNull() ?: return false
        val lastCorrectPath = lastAttempt.flatMap {
            listOf(
                it.whiteMove to it.whiteFeedback,
                it.blackMove to it.blackFeedback
            )
        }
        val currentInputPath =
            state.currentInputMoves.flatMap { listOf(it.whiteMove, it.blackMove) }
                .take(state.currentMoveCount)

        val isPathCorrectSoFar = currentInputPath.indices.all { i ->
            currentInputPath[i]?.notation == lastCorrectPath[i].first?.notation && lastCorrectPath[i].second == FeedbackType.CORRECT
        }
        return isPathCorrectSoFar && state.currentMoveCount < lastCorrectPath.size && lastCorrectPath[state.currentMoveCount].second == FeedbackType.CORRECT
    }

    private fun MutableStateFlow<ChessUiState>.updateWithFillCheck(function: (ChessUiState) -> ChessUiState) {
        this.update {
            val newState = function(it)
            newState.copy(isFillEnabled = checkFillEnabled(newState))
        }
    }

    private fun logSubmitEvent(name: String, won: Boolean, count: Int) {
        val bundle = Bundle().apply {
            putString("opening_name", name); putBoolean("is_correct", won); putInt(
            "attempt_count",
            count
        )
        }
        firebaseAnalytics.logEvent("submit_solution", bundle)
        if (won) firebaseAnalytics.logEvent("solve_success", bundle)
    }
}
