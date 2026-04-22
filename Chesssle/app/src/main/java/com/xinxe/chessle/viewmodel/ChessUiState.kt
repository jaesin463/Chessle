package com.xinxe.chessle.viewmodel

import com.xinxe.chessle.domain.model.MoveAttempt
import com.xinxe.chessle.domain.model.PieceColor
import com.xinxe.chessle.domain.model.Position
import com.xinxe.chessle.domain.model.Square

data class ChessUiState(
    val squares: List<Square> = emptyList(),
    val selectedSquare: Square? = null,
    val availableMoves: List<Position> = emptyList(),

    val currentTurn: PieceColor = PieceColor.WHITE,
    val currentInputMoves: List<MoveAttempt> = List(5) { MoveAttempt() },
    val currentMoveCount: Int = 0,

    val submittedAttempts: List<List<MoveAttempt>> = emptyList(),

    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,

    val isFillEnabled: Boolean = false, // Fill 버튼 활성화 여부 추가

    val openingName: String = "",
    val solutionText: String = "",
    val solution: List<String> = emptyList(),

    // [추가] 힌트로 밝혀진 정답 기보 (인덱스 0~9 -> 기보 문자열)
    val revealedHints: Map<Int, String> = emptyMap(),
    // [추가] 현재 판에서 힌트 사용 여부
    val dailyUsedHint: Boolean = false

)