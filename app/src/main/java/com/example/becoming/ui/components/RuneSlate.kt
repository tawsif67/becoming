package com.example.becoming.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class ClassType { KNIGHT, MAGE, ARTISAN, RANGER }

@Composable
fun RuneSlate(
    modifier: Modifier = Modifier,
    streakStatus: List<Boolean>, // Expecting size 7
    classTheme: ClassType
) {
    // Ensure we have 7 statuses, fill with false if shorter
    val statuses = remember(streakStatus) {
        streakStatus.take(7).let { if (it.size < 7) it + List(7 - it.size) { false } else it }
    }

    val themeColor = when (classTheme) {
        ClassType.KNIGHT -> Color(0xFFDC143C) // Crimson
        ClassType.MAGE -> Color(0xFF00FFFF)   // Cyan
        ClassType.ARTISAN -> Color(0xFFFFD700) // Gold
        ClassType.RANGER -> Color(0xFF50C878) // Emerald
    }

    val infiniteTransition = rememberInfiniteTransition(label = "runePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        statuses.forEach { isActive ->
            RuneItem(
                modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp),
                isActive = isActive,
                color = themeColor,
                classTheme = classTheme,
                alphaPulse = pulse,
                scalePulse = scalePulse
            )
        }
    }
}

@Composable
private fun RuneItem(
    modifier: Modifier,
    isActive: Boolean,
    color: Color,
    classTheme: ClassType,
    alphaPulse: Float,
    scalePulse: Float
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val shapeSize = size.minDimension * 0.8f
        
        translate(left = (size.width - shapeSize) / 2, top = (size.height - shapeSize) / 2) {
            val drawBlock: DrawScope.() -> Unit = {
                val path = getRunePath(classTheme, Size(shapeSize, shapeSize))
                if (isActive) {
                    // Glow effect: background soft fill
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.2f * alphaPulse)
                    )
                    // Solid fill
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.6f + (0.4f * alphaPulse))
                    )
                    // Border
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
                    )
                } else {
                    // Dimmed outline
                    drawPath(
                        path = path,
                        color = Color.Gray.copy(alpha = 0.3f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            if (isActive) {
                scale(scalePulse) {
                    drawBlock()
                }
            } else {
                drawBlock()
            }
        }
    }
}

private fun getRunePath(classTheme: ClassType, size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        when (classTheme) {
            ClassType.KNIGHT -> { // Shield
                moveTo(w * 0.5f, 0f)
                lineTo(w, h * 0.2f)
                lineTo(w, h * 0.6f)
                cubicTo(w, h * 0.85f, w * 0.7f, h, w * 0.5f, h)
                cubicTo(w * 0.3f, h, 0f, h * 0.85f, 0f, h * 0.6f)
                lineTo(0f, h * 0.2f)
                close()
            }
            ClassType.MAGE -> { // Diamond
                moveTo(w * 0.5f, 0f)
                lineTo(w, h * 0.5f)
                lineTo(w * 0.5f, h)
                lineTo(0f, h * 0.5f)
                close()
            }
            ClassType.ARTISAN -> { // Hexagon
                moveTo(w * 0.5f, 0f)
                lineTo(w, h * 0.25f)
                lineTo(w, h * 0.75f)
                lineTo(w * 0.5f, h)
                lineTo(0f, h * 0.75f)
                lineTo(0f, h * 0.25f)
                close()
            }
            ClassType.RANGER -> { // Leaf/Drop
                moveTo(w * 0.5f, 0f)
                cubicTo(w * 0.8f, h * 0.3f, w, h * 0.6f, w, h * 0.75f)
                cubicTo(w, h * 0.9f, w * 0.8f, h, w * 0.5f, h)
                cubicTo(w * 0.2f, h, 0f, h * 0.9f, 0f, h * 0.75f)
                cubicTo(0f, h * 0.6f, w * 0.2f, h * 0.3f, w * 0.5f, 0f)
                close()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewRuneSlate() {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        RuneSlate(
            streakStatus = listOf(true, true, true, false, false, false, false),
            classTheme = ClassType.KNIGHT
        )
        Spacer(Modifier.height(16.dp))
        RuneSlate(
            streakStatus = listOf(true, true, true, true, true, false, false),
            classTheme = ClassType.MAGE
        )
        Spacer(Modifier.height(16.dp))
        RuneSlate(
            streakStatus = listOf(true, false, true, false, true, false, true),
            classTheme = ClassType.ARTISAN
        )
        Spacer(Modifier.height(16.dp))
        RuneSlate(
            streakStatus = listOf(true, true, true, true, true, true, true),
            classTheme = ClassType.RANGER
        )
    }
}
