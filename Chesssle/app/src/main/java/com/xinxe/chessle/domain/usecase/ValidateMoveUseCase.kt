package com.xinxe.chessle.domain.usecase

import com.xinxe.chessle.domain.model.Piece
import com.xinxe.chessle.domain.model.PieceColor
import com.xinxe.chessle.domain.model.PieceType
import com.xinxe.chessle.domain.model.Position
import com.xinxe.chessle.domain.model.Square

object ValidateMoveUseCase {

    fun getValidMoves(square: Square, board: List<Square>): List<Position> {
        val piece = square.piece ?: return emptyList()

        return when (piece.type) {
            PieceType.PAWN -> getPawnMoves(square, board)
            PieceType.ROOK -> getRookMoves(square, board)
            PieceType.KNIGHT -> getKnightMoves(square, board)
            PieceType.BISHOP -> getBishopMoves(square, board)
            PieceType.QUEEN -> getQueenMoves(square, board)
            PieceType.KING -> getKingMoves(square, board)
        }
    }

    private fun getPawnMoves(square: Square, board: List<Square>): List<Position> {
        val moves = mutableListOf<Position>()
        val piece = square.piece ?: return emptyList()
        val direction = if (piece.color == PieceColor.WHITE) -1 else 1

        // 1. 한 칸 전진
        val nextRank = square.rank + direction
        if (nextRank in 0..7 && getPieceAt(nextRank, square.file, board) == null) {
            moves.add(Position(nextRank, square.file))

            // 2. 첫 이동 시 두 칸 전진 (앞이 비어있어야 함)
            val doubleNextRank = square.rank + (2 * direction)
            if (!piece.hasMoved && getPieceAt(doubleNextRank, square.file, board) == null) {
                moves.add(Position(doubleNextRank, square.file))
            }
        }

        // 3. 대각선 잡기
        listOf(square.file - 1, square.file + 1).forEach { f ->
            if (f in 0..7) {
                val target = getPieceAt(nextRank, f, board)
                if (target != null && target.color != piece.color) moves.add(Position(nextRank, f))
            }
        }
        return moves
    }

    private fun getRookMoves(square: Square, board: List<Square>): List<Position> {
        return getLinearMoves(
            square,
            board,
            listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
        )
    }

    private fun getBishopMoves(square: Square, board: List<Square>): List<Position> {
        return getLinearMoves(
            square,
            board,
            listOf(Pair(1, 1), Pair(-1, 1), Pair(-1, -1), Pair(1, -1))
        )
    }

    private fun getQueenMoves(square: Square, board: List<Square>): List<Position> {
        return getRookMoves(square, board) + getBishopMoves(square, board)
    }

    /**
     * 직선/대각선 이동을 위한 공통 로직
     */
    private fun getLinearMoves(
        square: Square,
        board: List<Square>,
        directions: List<Pair<Int, Int>>
    ): List<Position> {
        val moves = mutableListOf<Position>()
        val piece = square.piece ?: return emptyList()

        for (dir in directions) {
            var currRank = square.rank + dir.first
            var currFile = square.file + dir.second

            while (currRank in 0..7 && currFile in 0..7) {
                val target = getPieceAt(currRank, currFile, board)
                if (target == null) {
                    moves.add(Position(currRank, currFile))
                } else {
                    if (target.color != piece.color) moves.add(Position(currRank, currFile))
                    break // 기물이 있으면 더 못 감
                }
                currRank += dir.first
                currFile += dir.second
            }
        }
        return moves
    }

    private fun getKnightMoves(square: Square, board: List<Square>): List<Position> {
        val offsets = listOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        return calculateOffsetMoves(square, board, offsets)
    }

    private fun getKingMoves(square: Square, board: List<Square>): List<Position> {
        val offsets = listOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, -1),
            Pair(0, 1), Pair(1, -1), Pair(1, 0), Pair(1, 1)
        )
        val moves = calculateOffsetMoves(square, board, offsets).toMutableList()

        // --- 캐슬링 로직 ---
        val piece = square.piece ?: return moves
        if (!piece.hasMoved) {
            // 킹사이드 캐슬링 (오른쪽 룩)
            checkCastling(square, board, rookFile = 7, emptyFiles = listOf(5, 6))?.let { moves.add(it) }
            // 퀸사이드 캐슬링 (왼쪽 룩)
            checkCastling(square, board, rookFile = 0, emptyFiles = listOf(1, 2, 3))?.let { moves.add(it) }
        }
        return moves
    }

    private fun checkCastling(kingSquare: Square, board: List<Square>, rookFile: Int, emptyFiles: List<Int>): Position? {
        val rookSquare = board.find { it.rank == kingSquare.rank && it.file == rookFile }
        val rook = rookSquare?.piece

        // 1. 룩이 존재하고 움직인 적이 없어야 함
        if (rook != null && rook.type == PieceType.ROOK && !rook.hasMoved) {
            // 2. 사이 공간이 비어있어야 함
            val isPathClear = emptyFiles.all { f -> getPieceAt(kingSquare.rank, f, board) == null }
            if (isPathClear) {
                // 주의: 실제 체스에서는 '킹이 지나는 길이 공격받는지'도 체크해야 하지만,
                // 지금은 기초 단계이므로 위치 이동 가능 여부만 체크합니다.
                return Position(kingSquare.rank, if (rookFile == 7) 6 else 2)
            }
        }
        return null
    }

    private fun calculateOffsetMoves(square: Square, board: List<Square>, offsets: List<Pair<Int, Int>>): List<Position> {
        val moves = mutableListOf<Position>()
        val piece = square.piece ?: return emptyList()
        offsets.forEach { offset ->
            val r = square.rank + offset.first
            val f = square.file + offset.second
            if (r in 0..7 && f in 0..7) {
                val target = getPieceAt(r, f, board)
                if (target == null || target.color != piece.color) moves.add(Position(r, f))
            }
        }
        return moves
    }

    /**
     * 특정 색상의 모든 유효 이동 목록을 가져옵니다.
     */
    fun getValidMovesForColor(color: PieceColor, board: List<Square>): List<Pair<Square, Position>> {
        val allMoves = mutableListOf<Pair<Square, Position>>()
        board.filter { it.piece?.color == color }.forEach { square ->
            getValidMoves(square, board).forEach { position ->
                allMoves.add(square to position)
            }
        }
        return allMoves
    }

    /**
     * 특정 색상의 왕이 현재 체크 상태인지 확인합니다.
     */
    fun isInCheck(color: PieceColor, board: List<Square>): Boolean {
        val kingSquare = board.find { it.piece?.type == PieceType.KING && it.piece?.color == color }
            ?: return false

        // 상대방의 색상을 결정
        val opponentColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE

        // 상대방의 모든 기물이 현재 왕의 위치를 공격할 수 있는지 확인
        // 주의: 무한 루프를 방지하기 위해 여기서는 단순 이동 가능 여부만 체크하는 로직이 필요할 수 있음
        return board.filter { it.piece?.color == opponentColor }.any { square ->
            getRawMoves(square, board).any { it.rank == kingSquare.rank && it.file == kingSquare.file }
        }
    }

    /**
     * 킹이 안전한지 여부를 따지지 않는 '순수 기물 이동 규칙'만 반환합니다.
     * (체크 판정 시 무한 루프 방지용)
     */
    private fun getRawMoves(square: Square, board: List<Square>): List<Position> {
        val piece = square.piece ?: return emptyList()
        return when (piece.type) {
            PieceType.PAWN -> {
                // 폰은 잡을 때의 이동만 공격 범위로 간주함
                val moves = mutableListOf<Position>()
                val direction = if (piece.color == PieceColor.WHITE) -1 else 1
                val nextRank = square.rank + direction
                listOf(square.file - 1, square.file + 1).forEach { f ->
                    if (nextRank in 0..7 && f in 0..7) moves.add(Position(nextRank, f))
                }
                moves
            }
            PieceType.ROOK -> getRookMoves(square, board)
            PieceType.KNIGHT -> getKnightMoves(square, board)
            PieceType.BISHOP -> getBishopMoves(square, board)
            PieceType.QUEEN -> getQueenMoves(square, board)
            PieceType.KING -> {
                // 킹의 기본 8방향 이동 (캐슬링 제외)
                val offsets = listOf(
                    Pair(-1, -1), Pair(-1, 0), Pair(-1, 1), Pair(0, -1),
                    Pair(0, 1), Pair(1, -1), Pair(1, 0), Pair(1, 1)
                )
                val moves = mutableListOf<Position>()
                offsets.forEach { (r, f) ->
                    val nr = square.rank + r
                    val nf = square.file + f
                    if (nr in 0..7 && nf in 0..7) moves.add(Position(nr, nf))
                }
                moves
            }
        }
    }

    // 헬퍼 함수: 특정 위치의 기물 확인
    private fun getPieceAt(rank: Int, file: Int, board: List<Square>): Piece? {
        return board.find { it.rank == rank && it.file == file }?.piece
    }
}