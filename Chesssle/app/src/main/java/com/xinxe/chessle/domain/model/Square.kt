package com.xinxe.chessle.domain.model

import kotlinx.serialization.Serializable

/**
 * 체스판의 한 칸을 정의하는 데이터 클래스
 * @param rank 행 (0~7, 보통 체스에서는 1~8)
 * @param file 열 (0~7, 보통 체스에서는 a~h)
 * @param isDarkSquare 칸의 색상이 어두운지 여부
 * @param piece 현재 칸에 있는 기물 (없으면 null)
 */
@Serializable
data class Square(
    val rank: Int,
    val file: Int,
    val isDarkSquare: Boolean,
    val piece: Piece? = null // 기물 정보는 나중에 확장
)

// 좌표 관리를 위한 간단한 데이터 클래스
@Serializable
data class Position(val rank: Int = 0, val file: Int = 0)