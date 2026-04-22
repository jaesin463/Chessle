package com.xinxe.chessle.ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun RainyEffect() {
    // 빗줄기 데이터 초기화
    val rainDrops = remember {
        List(100) {
            RainDrop(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f,
                speed = Random.nextFloat() * 0.8f + 0.5f,
                length = Random.nextFloat() * 30f + 20f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        rainDrops.forEach { drop ->
            val currentYFraction = (drop.y + progress * drop.speed) % 1f

            val startX = drop.x * size.width
            val startY = currentYFraction * size.height

            drawLine(
                color = Color.DarkGray.copy(alpha = 0.5f),
                start = Offset(startX, startY),
                end = Offset(startX, startY + drop.length),
                strokeWidth = 3f
            )
        }
    }
}

private data class RainDrop(
    val x: Float,
    val y: Float,
    val speed: Float,
    val length: Float
)