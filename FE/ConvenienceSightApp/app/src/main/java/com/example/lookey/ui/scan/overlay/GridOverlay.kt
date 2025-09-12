package com.example.lookey.ui.scan.overlay


import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 카메라 위에 덧씌우는 가이드 격자(예: 3열 x 5행)
 */
@Composable
fun GridOverlay(
    modifier: Modifier = Modifier,
    cols: Int = 3,
    rows: Int = 5,
    lineWidth: Dp = 1.dp,
    lineColor: Color = Color.White.copy(alpha = 0.7f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val colW = w / cols
        val rowH = h / rows
        val stroke = Stroke(width = lineWidth.toPx(), cap = StrokeCap.Round)

        // 세로선
        for (i in 1 until cols) {
            val x = colW * i
            drawLine(lineColor, Offset(x, 0f), Offset(x, h), strokeWidth = stroke.width)
        }
        // 가로선
        for (j in 1 until rows) {
            val y = rowH * j
            drawLine(lineColor, Offset(0f, y), Offset(w, y), strokeWidth = stroke.width)
        }
    }
}
