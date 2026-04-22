package com.xinxe.chessle.ui.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.xinxe.chessle.R
import com.xinxe.chessle.domain.model.*
import com.xinxe.chessle.ui.theme.MoveHint

@Composable
fun ChessBoard(
    squares: List<Square>,
    availableMoves: List<Position>,
    onSquareClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 테마 시스템 색상 매핑
    val darkSquareColor = MaterialTheme.colorScheme.primary
    val lightSquareColor = MaterialTheme.colorScheme.secondary
    val hintColor = MoveHint

    BoxWithConstraints(modifier = modifier) {
        val squareSize = maxWidth / 8
        // 기물이 칸에 꽉 차지 않도록 약간의 여백(Padding)을 줍니다.
        val piecePadding = squareSize * 0.15f
        val dotSize = squareSize * 0.4f

        Column(modifier = Modifier.fillMaxSize()) {
            for (rank in 0 until 8) {
                Row(modifier = Modifier.weight(1f)) {
                    for (file in 0 until 8) {
                        val square = squares.find { it.rank == rank && it.file == file } ?: continue
                        val isPossibleMove = availableMoves.any { it.rank == rank && it.file == file }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(if (square.isDarkSquare) darkSquareColor else lightSquareColor)
                                .clickable { onSquareClick(rank, file) },
                            contentAlignment = Alignment.Center
                        ) {
                            // 1. 기물 표시 (XML 리소스 반영)
                            square.piece?.let { piece ->
                                PieceImage(
                                    piece = piece,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(piecePadding)
                                )
                            }

                            // 2. 이동 가능 힌트 (반투명 점)
                            if (isPossibleMove) {
                                Surface(
                                    modifier = Modifier.size(dotSize),
                                    color = hintColor.copy(alpha = 0.6f),
                                    shape = CircleShape
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieceImage(piece: Piece, modifier: Modifier = Modifier) {
    // 기물 종류와 색상에 맞는 리소스 ID 선택
    val resId = when (piece.color) {
        PieceColor.WHITE -> when (piece.type) {
            PieceType.PAWN -> R.drawable.ic_white_pawn
            PieceType.ROOK -> R.drawable.ic_white_rook
            PieceType.KNIGHT -> R.drawable.ic_white_knight
            PieceType.BISHOP -> R.drawable.ic_white_bishop
            PieceType.QUEEN -> R.drawable.ic_white_queen
            PieceType.KING -> R.drawable.ic_white_king
        }
        PieceColor.BLACK -> when (piece.type) {
            PieceType.PAWN -> R.drawable.ic_black_pawn
            PieceType.ROOK -> R.drawable.ic_black_rook
            PieceType.KNIGHT -> R.drawable.ic_black_knight
            PieceType.BISHOP -> R.drawable.ic_black_bishop
            PieceType.QUEEN -> R.drawable.ic_black_queen
            PieceType.KING -> R.drawable.ic_black_king
        }
    }

    Image(
        painter = painterResource(id = resId),
        contentDescription = "${piece.color} ${piece.type}",
        modifier = modifier,
        // XML에 이미 고유 색상이 정의되어 있으므로 ColorFilter는 적용하지 않습니다.
        colorFilter = null
    )
}