package com.xinxe.chessle.domain.usecase

import com.xinxe.chessle.domain.model.*
import kotlin.math.abs

object MovePieceUseCase {
    fun execute(from: Square, to: Square, currentSquares: List<Square>): Pair<List<Square>, Move> {
        val originalPiece = from.piece ?: throw IllegalStateException()
        val movingPiece = from.piece.copy(hasMoved = true)
        val capturedPiece = currentSquares.find { it.rank == to.rank && it.file == to.file }?.piece
        val isCastling = movingPiece.type == PieceType.KING && abs(from.file - to.file) == 2

        val notation = createNotation(from, to, currentSquares)

        val newSquares = applyMoveLogic(from, to, currentSquares, movingPiece, isCastling)

        val move = Move(
            from = Position(from.rank, from.file),
            to = Position(to.rank, to.file),
            piece = movingPiece,
            originalPiece = originalPiece, // 이동 전 상태(hasMoved=false일 확률 높음) 저장
            notation = notation,
            capturedPiece = capturedPiece,
            isCastling = isCastling
        )

        return Pair(newSquares, move)
    }

    private fun applyMoveLogic(
        from: Square,
        to: Square,
        currentSquares: List<Square>,
        movingPiece: Piece,
        isCastling: Boolean
    ): List<Square> {
        var newSquares = currentSquares.map { s ->
            when {
                s.rank == from.rank && s.file == from.file -> s.copy(piece = null)
                s.rank == to.rank && s.file == to.file -> s.copy(piece = movingPiece)
                else -> s
            }
        }

        if (isCastling) {
            val isKingSide = to.file == 6
            val oldFile = if (isKingSide) 7 else 0
            val newFile = if (isKingSide) 5 else 3
            newSquares = newSquares.map { s ->
                when {
                    s.rank == to.rank && s.file == oldFile -> s.copy(piece = null)
                    s.rank == to.rank && s.file == newFile -> s.copy(
                        piece = Piece(PieceType.ROOK, movingPiece.color, true)
                    )
                    else -> s
                }
            }
        }
        return newSquares
    }

    private fun createNotation(from: Square, to: Square, board: List<Square>): String {
        val p = from.piece ?: return ""

        if (p.type == PieceType.KING && abs(from.file - to.file) == 2) {
            return if (to.file == 6) "O-O" else "O-O-O"
        }

        val isCap = board.find { it.rank == to.rank && it.file == to.file }?.piece != null
        val dest = "${"abcdefgh"[to.file]}${8 - to.rank}"
        val prefix = when (p.type) {
            PieceType.PAWN -> ""
            PieceType.ROOK -> "R"
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
        }

        var disambiguation = ""
        if (p.type != PieceType.PAWN && p.type != PieceType.KING) {
            val candidates = board.filter {
                it.piece?.type == p.type &&
                        it.piece.color == p.color &&
                        it != from &&
                        ValidateMoveUseCase.getValidMoves(it, board).contains(Position(to.rank, to.file))
            }

            if (candidates.isNotEmpty()) {
                val sameFile = candidates.any { it.file == from.file }
                val sameRank = candidates.any { it.rank == from.rank }
                disambiguation = when {
                    !sameFile -> "abcdefgh"[from.file].toString()
                    !sameRank -> (8 - from.rank).toString()
                    else -> "${"abcdefgh"[from.file]}${8 - from.rank}"
                }
            }
        }

        val baseMove = if (p.type == PieceType.PAWN) {
            if (isCap) "${"abcdefgh"[from.file]}x$dest" else dest
        } else {
            val captureSign = if (isCap) "x" else ""
            "$prefix$disambiguation$captureSign$dest"
        }

        val movingPiece = p.copy(hasMoved = true)
        val isCastling = p.type == PieceType.KING && abs(from.file - to.file) == 2
        val nextBoard = applyMoveLogic(from, to, board, movingPiece, isCastling)

        val opponentColor = if (p.color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        val isCheck = ValidateMoveUseCase.isInCheck(opponentColor, nextBoard)
        val isMate = isCheck && ValidateMoveUseCase.getValidMovesForColor(opponentColor, nextBoard).isEmpty()

        return when {
            isMate -> "${baseMove}#"
            isCheck -> "${baseMove}+"
            else -> baseMove
        }
    }
}