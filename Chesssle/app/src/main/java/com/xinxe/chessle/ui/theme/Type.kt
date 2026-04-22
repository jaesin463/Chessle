package com.xinxe.chessle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // [Display/Headline] - 로고 및 큰 강조 제목
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp
    ),
    headlineSmall = TextStyle( // 다이얼로그 타이틀용
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp
    ),

    // [Title] - 섹션 제목, 강조 숫자
    titleLarge = TextStyle( // 상단바 로고 텍스트용
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        letterSpacing = 1.5.sp
    ),
    titleMedium = TextStyle( // 스트릭 숫자, 섹션 소제목용
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),

    // [Body] - 일반 텍스트 및 정보
    bodyLarge = TextStyle( // 메인 본문, 기보 컨트롤러 텍스트
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle( // 보조 설명, 규칙 내용
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle( // 오프닝 이름, 강조 팁 전용
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    ),

    // [Label] - 버튼 및 특수 배지
    labelLarge = TextStyle( // 버튼용
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    ),
    labelSmall = TextStyle( // 기보 배지(Monospace), 상태 텍스트
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )
)