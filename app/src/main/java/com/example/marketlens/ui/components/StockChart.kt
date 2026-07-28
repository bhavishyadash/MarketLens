package com.example.marketlens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketlens.ui.theme.MonoFamily
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp
import com.example.marketlens.ui.theme.TerminalBorder

@Composable
fun StockChart(
    prices:   List<Double>,
    modifier: Modifier = Modifier
) {
    if (prices.size < 2) {
        Box(modifier.height(200.dp))
        return
    }

    val isPositive = prices.last() >= prices.first()
    val lineColor  = if (isPositive) PriceUp else PriceDown
    val gridColor  = TerminalBorder
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val textStyle = TextStyle(
        fontFamily    = MonoFamily,
        fontSize      = 9.sp,
        color         = labelColor,
        letterSpacing = 0.5.sp
    )

    val minPrice   = prices.min()
    val maxPrice   = prices.max()
    val priceRange = (maxPrice - minPrice).takeIf { it > 0 } ?: 1.0

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width  = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        val labelAreaWidth = 52f
        val chartWidth     = width - labelAreaWidth
        val paddingTop     = 14f
        val paddingBot     = 20f
        val chartH         = height - paddingTop - paddingBot

        // Dotted horizontal grid (5 lines) — terminal chart chrome
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f), 0f)
        for (i in 0..4) {
            val y = paddingTop + chartH * (i / 4f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end   = Offset(chartWidth, y),
                strokeWidth = 1f,
                pathEffect  = dashEffect
            )
        }

        // Vertical guide at the right edge
        drawLine(
            color = gridColor,
            start = Offset(chartWidth, paddingTop),
            end   = Offset(chartWidth, paddingTop + chartH),
            strokeWidth = 1f
        )

        fun priceToOffset(index: Int, price: Double): Offset {
            val x          = (index.toFloat() / (prices.size - 1)) * chartWidth
            val normalized = (price - minPrice) / priceRange
            val y          = paddingTop + chartH * (1f - normalized.toFloat())
            return Offset(x, y)
        }

        val points = prices.mapIndexed { i, price -> priceToOffset(i, price) }

        val fillPath = Path().apply {
            moveTo(points.first().x, paddingTop + chartH)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, paddingTop + chartH)
            close()
        }

        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = paddingTop,
                endY   = paddingTop + chartH
            )
        )

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        drawPath(
            path  = linePath,
            color = lineColor,
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val lastPoint = points.last()
        // Glow ring — subtle neon
        drawCircle(color = lineColor.copy(alpha = 0.25f), radius = 10f, center = lastPoint)
        drawCircle(color = lineColor, radius = 4f, center = lastPoint)
        drawCircle(color = Color.Black, radius = 1.5f, center = lastPoint)

        if (labelAreaWidth > 0f) {
            val labelX = chartWidth + 6f
            drawYLabel(textMeasurer, textStyle, "%.2f".format(maxPrice), labelX, paddingTop)
            drawYLabel(textMeasurer, textStyle, "%.2f".format((minPrice + maxPrice) / 2), labelX, paddingTop + chartH / 2)
            drawYLabel(textMeasurer, textStyle, "%.2f".format(minPrice), labelX, paddingTop + chartH - 10f)
        }
    }
}

private fun DrawScope.drawYLabel(
    textMeasurer: TextMeasurer,
    style:        TextStyle,
    text:         String,
    x:            Float,
    y:            Float
) {
    if (x >= size.width) return
    drawText(
        textMeasurer = textMeasurer,
        text         = text,
        style        = style,
        topLeft      = Offset(x, y - 6f)
    )
}
