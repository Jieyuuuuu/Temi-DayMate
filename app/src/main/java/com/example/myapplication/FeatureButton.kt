package com.example.myapplication

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas

@Composable
fun FeatureButton(feature: FeatureDestination, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 120), label = "buttonScale"
    )
    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .clickable(
                enabled = true,
                onClickLabel = feature.name,
                onClick = {
                    pressed = true
                    onClick()
                    pressed = false
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(40.dp)) {
                when (feature.iconType) {
                    0 -> drawCircle(Color(0xFF007AFF)) // blue circle
                    1 -> drawRect(Color(0xFF34C759)) // green square
                    2 -> drawPath(androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width/2, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }, Color(0xFFFF9500)) // orange triangle
                    3 -> drawStar(Color(0xFFFF2D55)) // pink star
                    4 -> drawHeart(Color(0xFFAF52DE)) // purple heart
                    5 -> drawOval(Color(0xFF5AC8FA)) // blue oval
                    6 -> drawDiamond(Color(0xFFFFCC00)) // yellow diamond
                    7 -> drawHexagon(Color(0xFF5856D6)) // deep purple hexagon
                    8 -> drawSemiCircle(Color(0xFFFF3B30)) // red semicircle
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = feature.name,
                fontSize = 14.sp,
                color = Color(0xFF222B45)
            )
        }
    }
}

// Geometry draw helpers
fun DrawScope.drawStar(color: Color) {
    val path = androidx.compose.ui.graphics.Path()
    val midX = size.width / 2
    val midY = size.height / 2
    val r = size.minDimension / 2
    for (i in 0..4) {
        val angle = Math.toRadians((i * 72 - 90).toDouble())
        val x = midX + r * Math.cos(angle).toFloat()
        val y = midY + r * Math.sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        val angle2 = Math.toRadians(((i + 0.5) * 72 - 90).toDouble())
        val x2 = midX + r * 0.5f * Math.cos(angle2).toFloat()
        val y2 = midY + r * 0.5f * Math.sin(angle2).toFloat()
        path.lineTo(x2, y2)
    }
    path.close()
    drawPath(path, color)
}
fun DrawScope.drawHeart(color: Color) {
    val path = androidx.compose.ui.graphics.Path()
    val w = size.width
    val h = size.height
    path.moveTo(w/2, h*0.8f)
    path.cubicTo(w*1.1f, h*0.5f, w*0.8f, h*0.05f, w/2, h*0.3f)
    path.cubicTo(w*0.2f, h*0.05f, -w*0.1f, h*0.5f, w/2, h*0.8f)
    path.close()
    drawPath(path, color)
}
fun DrawScope.drawDiamond(color: Color) {
    val path = androidx.compose.ui.graphics.Path()
    path.moveTo(size.width/2, 0f)
    path.lineTo(size.width, size.height/2)
    path.lineTo(size.width/2, size.height)
    path.lineTo(0f, size.height/2)
    path.close()
    drawPath(path, color)
}
fun DrawScope.drawHexagon(color: Color) {
    val path = androidx.compose.ui.graphics.Path()
    val r = size.minDimension / 2
    val centerX = size.width / 2
    val centerY = size.height / 2
    for (i in 0..5) {
        val angle = Math.toRadians((60 * i - 30).toDouble())
        val x = centerX + r * Math.cos(angle).toFloat()
        val y = centerY + r * Math.sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}
fun DrawScope.drawSemiCircle(color: Color) {
    drawArc(color, 180f, 180f, useCenter = true, topLeft = androidx.compose.ui.geometry.Offset(0f,0f), size = size)
} 