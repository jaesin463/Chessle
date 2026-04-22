package com.xinxe.chessle.domain.model

object BoardFactory {
    fun createInitialBoard() = List(64) { i ->
        Square(
            i / 8,
            i % 8,
            (i / 8 + i % 8) % 2 != 0,
            getInitialPiece(i / 8, i % 8)
        )
    }

    fun getInitialPiece(rank: Int, file: Int): Piece? {
        val color =
            if (rank <= 1) PieceColor.BLACK else if (rank >= 6) PieceColor.WHITE else return null
        return when (rank) {
            1, 6 -> Piece(PieceType.PAWN, color)
            0, 7 -> when (file) {
                0, 7 -> Piece(PieceType.ROOK, color)
                1, 6 -> Piece(PieceType.KNIGHT, color)
                2, 5 -> Piece(PieceType.BISHOP, color)
                3 -> Piece(PieceType.QUEEN, color)
                4 -> Piece(PieceType.KING, color)
                else -> null
            }

            else -> null
        }
    }
}