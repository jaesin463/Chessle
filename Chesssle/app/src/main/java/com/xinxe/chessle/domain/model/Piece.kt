package com.xinxe.chessle.domain.model

import kotlinx.serialization.Serializable

/**
 * 기물 데이터 클래스
 */
@Serializable
data class Piece (
    val type: PieceType = PieceType.PAWN,
    val color: PieceColor = PieceColor.WHITE,
    val hasMoved: Boolean = false
)

/**
 * 기물 진영
 * WHITE, BLACK
 */
@Serializable
enum class PieceColor {
    WHITE, BLACK
}

/**
 * 기물 종류
 * KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
 */
@Serializable
enum class PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
}