package com.xinxe.chessle.ui.theme

import androidx.compose.ui.graphics.Color

// 1. 브랜드 & 메인 컬러
val ChessGreen = Color(0xFF769656) // 성공, 정답(Correct), 제출 버튼
val ChessDarkGreen = Color(0xFF537133)
val ChessCream = Color(0xFFEEEED2) // 라이트 모드용 배경 보조

// 2. 체스보드 전용 (BoardSection)
val BoardLight = Color(0xFFEEFEED)
val BoardDark = ChessGreen // 브랜드 컬러 재사용
val MoveHint = Color(0x33000000)

// 3. 시스템 피드백 (워들 스타일 그리드)
val FeedbackCorrect = ChessGreen
val FeedbackPresent = Color(0xFFC9B458) // 노랑 (위치 틀림)
val FeedbackAbsent = Color(0xFF3A3A3C)  // 진회색 (없음)
val RuleWrong = Color(0xFFF44336)      // 강조 에러 (빨간색)

// 4. 배경 및 그레이스케일
val ModalDeepGreen = Color(0xFF07140D)
val DarkGrey = ModalDeepGreen          // 메인 배경
val SurfaceGrey = ModalDeepGreen       // 다이얼로그, 카드 배경
val NotationBadgeBg = Color(0xFF424242) // 기보 배지

// 5. 포인트 컬러
val GoldAccent = Color(0xFFC9B458)     // 오프닝 이름, 스트릭 활성화 강조
val StreakInactive = Color(0xFF999999)

val ShareBackground = Color(0xFF121213)
