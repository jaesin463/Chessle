package com.xinxe.chessle.util

import com.xinxe.chessle.domain.model.PieceColor
import com.xinxe.chessle.domain.model.PieceType
import com.xinxe.chessle.domain.model.Square
import com.xinxe.chessle.domain.usecase.ValidateMoveUseCase
import kotlin.collections.filter

object NotationFormatter {

    // 기보 리스트를 "1. Nf3 d5..." 형태로 변환하는 보조 함수
    fun format(solution: List<String>): String {
        val sb = StringBuilder()
        for (i in solution.indices step 2) {
            val moveNum = (i / 2) + 1
            sb.append("$moveNum. ${solution[i]}")
            if (i + 1 < solution.size) {
                sb.append(" ${solution[i + 1]} • ")
            }
        }
        return sb.toString().trim()
    }

    /**
     * @param notation "Nf3", "e4", "Bxe5" 등의 기보
     * @param squares 현재 보드의 상태
     * @return 출발 Square와 도착 Square의 Pair (찾지 못하면 null)
     */
    fun parse(notation: String, squares: List<Square>, isWhiteTurn: Boolean): Pair<Square, Square>? {
        if (notation == "O-O" || notation == "O-O-O") return handleCastling(notation, squares, isWhiteTurn)

        // 1. 특수 기호 제거 (x를 제거하기 전에 폰의 출발 파일 정보를 기억해야 함)
        val isCapture = notation.contains("x")
        val clean = notation.replace("+", "").replace("#", "")

        val targetColor = if (isWhiteTurn) PieceColor.WHITE else PieceColor.BLACK

        // 2. 목적지 추출
        val targetPosStr = clean.takeLast(2)
        val targetFile = targetPosStr[0] - 'a'
        // 중요: 프로젝트의 Rank가 0(상단/8행) ~ 7(하단/1행)이라면 아래와 같이 계산해야 함
        val targetRank = 8 - targetPosStr[1].digitToInt()

        val targetSquare = squares.find { it.file == targetFile && it.rank == targetRank } ?: return null

        // 3. 기물 종류 판별
        val firstChar = clean[0]
        val isPawn = !firstChar.isUpperCase()
        val pieceType = if (!isPawn) {
            when (firstChar) {
                'N' -> PieceType.KNIGHT
                'B' -> PieceType.BISHOP
                'R' -> PieceType.ROOK
                'Q' -> PieceType.QUEEN
                'K' -> PieceType.KING
                else -> PieceType.PAWN
            }
        } else {
            PieceType.PAWN
        }

        // 4. 출발지 후보 찾기
        val candidates = squares.filter { s ->
            val p = s.piece
            p != null &&
                    p.color == targetColor &&
                    p.type == pieceType &&
                    canMove(s, targetSquare, squares)
        }

        // 5. 모호성 해결 (특히 폰이 잡을 때 'exd5'에서 'e' 처리)
        val sourceSquare = if (candidates.size > 1) {
            // 기보에서 목적지(마지막 2글자)를 제외한 앞부분에서 힌트 추출
            val hintString = clean.dropLast(2)
            val hint = if (hintString.isNotEmpty()) {
                if (isPawn) hintString[0] else hintString.drop(1).firstOrNull()
            } else null

            candidates.find {
                when {
                    hint == null -> true
                    hint.isDigit() -> it.rank == (8 - hint.digitToInt())
                    else -> it.file == (hint - 'a')
                }
            } ?: candidates[0]
        } else {
            candidates.getOrNull(0)
        }

        return if (sourceSquare != null) Pair(sourceSquare, targetSquare) else null
    }

    private fun canMove(from: Square, to: Square, squares: List<Square>): Boolean {
        // 해당 칸의 기물이 갈 수 있는 모든 유효 좌표를 가져옴
        val validPositions = ValidateMoveUseCase.getValidMoves(from, squares)

        // 유효 좌표 중에 목적지(to)의 좌표가 있는지 확인
        return validPositions.any { it.rank == to.rank && it.file == to.file }
    }

    private fun handleCastling(notation: String, squares: List<Square>, isWhiteTurn: Boolean): Pair<Square, Square>? {
        // 캐슬링은 킹의 이동으로 처리 (White 기준 예시)
        // 실제로는 차례(Turn)를 확인하여 흑/백 킹 위치를 잡아야 합니다.
        val rank = if (isWhiteTurn) 7 else 0 // 백색은 7번행, 흑색은 0번행 (프로젝트 좌표계 기준)
        val fromFile = 4 // e열
        val toFile = if (notation == "O-O") 6 else 2 // g열 또는 c열

        val from = squares.find { it.rank == rank && it.file == fromFile }
        val to = squares.find { it.rank == rank && it.file == toFile }
        return if (from != null && to != null) Pair(from, to) else null
    }
}