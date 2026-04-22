package com.xinxe.chessle.domain.model

enum class FeedbackType {
    CORRECT,      // 초록색 (수 순서와 기보가 완벽히 일치)
    PRESENT,      // 노란색 (정답 시퀀스 어딘가에 이 기보가 존재함)
    ABSENT       // 회색 (정답 시퀀스에 없는 기보)
}