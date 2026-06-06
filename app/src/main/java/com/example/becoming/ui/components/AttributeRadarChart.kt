package com.example.becoming.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

data class CharacterAttributes(
    val intellect: Float,
    val endurance: Float,
    val strength: Float,
    val devotion: Float,
    val mindfulness: Float
)

@Composable
fun AttributeRadarChart(
    attributes: CharacterAttributes,
    classColor: Color,
    maxValue: Float = 100f
) {
    val traitValues = listOf(
        attributes.intellect,
        attributes.endurance,
        attributes.strength,
        attributes.devotion,
        attributes.mindfulness
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2
        val numPoints = 5

        // 1. Draw Background Grid (Concentric Pentagons)
        for (step in 1..5) {
            val gridRadius = maxRadius * (step / 5f)
            val gridPath = Path()
            for (i in 0 until numPoints) {
                val angle = -(Math.PI / 2) + i * (2 * Math.PI / numPoints)
                val x = centerX + gridRadius * cos(angle).toFloat()
                val y = centerY + gridRadius * sin(angle).toFloat()
                if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
            }
            gridPath.close()
            drawPath(
                path = gridPath,
                color = Color.White.copy(alpha = 0.1f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw Spokes (Connecting center to vertices)
        for (i in 0 until numPoints) {
            val angle = -(Math.PI / 2) + i * (2 * Math.PI / numPoints)
            val x = centerX + maxRadius * cos(angle).toFloat()
            val y = centerY + maxRadius * sin(angle).toFloat()
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 3. Calculate Attribute Polygon Vertices
        val attributePath = Path()
        val points = mutableListOf<Offset>()

        for (i in 0 until numPoints) {
            // TRIG LOGIC FROM SECTION 3B
            // Angle: θ = -π/2 + i * (2π/5)
            val theta = -(Math.PI.toFloat() / 2f) + i * (2 * Math.PI.toFloat() / 5f)
            // Radius: r = r_max * (trait_value / max_value)
            val r = maxRadius * (traitValues[i] / maxValue)
            // X Coordinate: x = c_x + r * cos(θ)
            val x = centerX + r * cos(theta)
            // Y Coordinate: y = c_y + r * sin(θ)
            val y = centerY + r * sin(theta)
            
            val point = Offset(x, y)
            points.add(point)
            
            if (i == 0) attributePath.moveTo(x, y) else attributePath.lineTo(x, y)
        }
        attributePath.close()

        // 4. Draw Attribute Area with Glow (Using Native Canvas for BlurMaskFilter)
        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint().apply {
                color = classColor.toArgb()
                style = android.graphics.Paint.Style.FILL
                maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            }
            
            // Draw Glow Layer
            canvas.nativeCanvas.drawPath(attributePath.asAndroidPath(), paint)
            
            // Draw Main Shape
            drawPath(
                path = attributePath,
                color = classColor.copy(alpha = 0.4f),
                style = Fill
            )
            
            // Draw Outline
            drawPath(
                path = attributePath,
                color = classColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 5. Draw Vertices (Nodes)
        points.forEach { point ->
            drawCircle(
                color = classColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}
