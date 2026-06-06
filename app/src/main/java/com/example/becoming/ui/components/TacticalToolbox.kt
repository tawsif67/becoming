package com.example.becoming.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.becoming.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LogisticsDial(
    label: String,
    unit: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text(label, color = AntiqueGold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.3f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(CrimsonRed, AntiqueGold)),
                    startAngle = 135f,
                    sweepAngle = 270f * (value / 50f),
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Compass markings
                for (i in 0..10) {
                    val angle = 135f + (i * 27f)
                    val rad = Math.toRadians(angle.toDouble())
                    val start = Offset(
                        (center.x + (size.width / 2 - 20.dp.toPx()) * cos(rad)).toFloat(),
                        (center.y + (size.width / 2 - 20.dp.toPx()) * sin(rad)).toFloat()
                    )
                    val end = Offset(
                        (center.x + (size.width / 2) * cos(rad)).toFloat(),
                        (center.y + (size.width / 2) * sin(rad)).toFloat()
                    )
                    drawLine(color = AntiqueGold, start = start, end = end, strokeWidth = 2.dp.toPx())
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${value.toInt()}", color = ParchmentCream, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text(unit, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..50f,
            colors = SliderDefaults.colors(thumbColor = AntiqueGold, activeTrackColor = AntiqueGold)
        )
    }
}

@Composable
fun ArchiveMeter(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(label, color = AntiqueGold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(10) { i ->
                val isFilled = i < (value / 5).toInt()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isFilled) AntiqueGold else Color.Black.copy(alpha = 0.3f))
                        .border(1.dp, AntiqueGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..50f,
            colors = SliderDefaults.colors(thumbColor = AntiqueGold, activeTrackColor = AntiqueGold)
        )
        Text("Target: ${value.toInt()} Items", color = ParchmentCream, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun HourglassGauge(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text(label, color = AntiqueGold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.size(100.dp, 150.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(0f, h)
                    lineTo(w, h)
                    close()
                }
                drawPath(path, color = Color.Gray.copy(alpha = 0.2f))
                
                // Sand filling logic simplified
                drawRect(
                    color = AntiqueGold.copy(alpha = 0.6f),
                    topLeft = Offset(0f, h * (1 - value / 120f)),
                    size = androidx.compose.ui.geometry.Size(w, h * (value / 120f))
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 5f..120f,
            colors = SliderDefaults.colors(thumbColor = AntiqueGold, activeTrackColor = AntiqueGold)
        )
        Text("${value.toInt()} Minutes", color = ParchmentCream)
    }
}

@Composable
fun LedgerToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ParchmentCream, style = MaterialTheme.typography.bodyLarge)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (checked) ForestGreen else Color.DarkGray)
                .border(2.dp, AntiqueGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ThresholdGauge(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(label, color = AntiqueGold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value / 100f)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(RoyalBlue, CrimsonRed)))
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..100f,
            colors = SliderDefaults.colors(thumbColor = AntiqueGold, activeTrackColor = Color.Transparent)
        )
        Text("Intensity: ${value.toInt()}%", color = ParchmentCream, modifier = Modifier.align(Alignment.End))
    }
}
