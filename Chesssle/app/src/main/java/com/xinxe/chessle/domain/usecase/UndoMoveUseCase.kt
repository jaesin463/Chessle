package com.xinxe.chessle.domain.usecase

import com.xinxe.chessle.domain.model.*

object UndoMoveUseCase {
    fun execute(currentSquares: List<Square>, lastMove: Move): List<Square> {
        // 1. 기본 기물 위치 복구 (originalPiece 사용)
        var restored = currentSquares.map { square ->
            when {
                // 원래 자리(from)로 '이동 전 기물' 복귀
                square.rank == lastMove.from.rank && square.file == lastMove.from.file ->
                    square.copy(piece = lastMove.originalPiece)

                // 이동했던 자리(to)는 잡혔던 기물로 복구
                square.rank == lastMove.to.rank && square.file == lastMove.to.file ->
                    square.copy(piece = lastMove.capturedPiece)

                else -> square
            }
        }

        // 2. 캐슬링 특수 상황 복구
        if (lastMove.isCastling) {
            val rank = lastMove.to.rank
            val isKingSide = lastMove.to.file == 6
            val oldRookFile = if (isKingSide) 7 else 0
            val curRookFile = if (isKingSide) 5 else 3

            restored = restored.map { s ->
                when {
                    s.rank == rank && s.file == curRookFile -> s.copy(piece = null)
                    // 룩 역시 originalPiece와 마찬가지로 hasMoved를 false로 돌려놓아야 함
                    s.rank == rank && s.file == oldRookFile ->
                        s.copy(piece = Piece(PieceType.ROOK, lastMove.piece.color, false))
                    else -> s
                }
            }
        }
        return restored
    }
}