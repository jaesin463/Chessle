package com.xinxe.chessle.domain.model

import kotlinx.serialization.Serializable

/**
 * 한 수의 정보를 담는 데이터 클래스
 */
@Serializable
data class Move(
    val from: Position = Position(0, 0),
    val to: Position = Position(0, 0),
    val piece: Piece = Piece(PieceType.PAWN, PieceColor.WHITE),
    val originalPiece: Piece = Piece(PieceType.PAWN, PieceColor.WHITE),
    val notation: String = "",
    val capturedPiece: Piece? = null,
    val isCastling: Boolean = false
)

@Serializable
data class MoveAttempt(
    val whiteMove: Move? = null,
    val blackMove: Move? = null,
    // 피드백 정보를 추가하여 UI에서 색상을 결정할 수 있게 함
    val whiteFeedback: FeedbackType = FeedbackType.ABSENT,
    val blackFeedback: FeedbackType = FeedbackType.ABSENT
)