package com.xinxe.chessle.ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun ConfettiEffect() {
    // 50개의 종이 가루 생성
    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = Random.nextFloat() * 1000f,
                y = Random.nextFloat() * -500f,
                color = Color(
                    Random.nextInt(256),
                    Random.nextInt(256),
                    Random.nextInt(256)
                ),
                speed = Random.nextFloat() * 500f + 500f,
                size = Random.nextFloat() * 20f + 10f
            )
        }
    }

    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = interimTween(3000) // 3초 동안 떨어짐
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val progress = animatable.value
            val currentY = particle.y + (particle.speed * progress * 5f)

            if (currentY < size.height) {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(particle.x * (size.width / 1000f), currentY),
                    size = Size(particle.size, particle.size)
                )
            }
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val speed: Float,
    val size: Float
)

private fun interimTween(duration: Int): AnimationSpec<Float> =
    tween(durationMillis = duration, easing = LinearEasing)