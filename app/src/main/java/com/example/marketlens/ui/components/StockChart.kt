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
import com.example.marketlens.ui.theme.PriceDown
import com.example.marketlens.ui.theme.PriceUp


@Composable
fun StockChart(
    prices:   List<Double>,
    modifier: Modifier = Modifier
) {
    if (prices.size < 2) {
        Box(modifier.height(180.dp))
        return
    }

    val isPositive = prices.last() >= prices.first()
    val lineColor  = if (isPositive) PriceUp else PriceDown
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val textStyle = TextStyle(
        fontSize = 10.sp,
        color    = labelColor
    )

    val minPrice   = prices.min()
    val maxPrice   = prices.max()
    val priceRange = (maxPrice - minPrice).takeIf { it > 0 } ?: 1.0

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width      = size.width
        val height     = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        val labelAreaWidth = 48f
        val chartWidth     = width - labelAreaWidth
        val paddingTop     = 12f
        val paddingBot     = 20f
        val chartH         = height - paddingTop - paddingBot

        fun priceToOffset(index: Int, price: Double): Offset {
            val x          = (index.toFloat() / (prices.size - 1)) * chartWidth
            val normalized = (price - minPrice) / priceRange
            val y          = paddingTop + chartH * (1f - normalized.toFloat())
            return Offset(x, y)
        }

        val points = prices.mapIndexed { i, price -> priceToOffset(i, price) }

        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, height)
            close()
        }

        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.30f), Color.Transparent),
                startY = paddingTop,
                endY   = height
            )
        )

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        drawPath(
            path  = linePath,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val lastPoint = points.last()
        drawCircle(color = lineColor,   radius = 5f,   center = lastPoint)
        drawCircle(color = Color.White, radius = 2.5f, center = lastPoint)

        if (labelAreaWidth > 0f) {
            val labelX = chartWidth + 4f
            drawYLabel(textMeasurer, textStyle, "$%.0f".format(maxPrice), labelX, paddingTop)
            drawYLabel(textMeasurer, textStyle, "$%.0f".format((minPrice + maxPrice) / 2), labelX, paddingTop + chartH / 2)
            drawYLabel(textMeasurer, textStyle, "$%.0f".format(minPrice), labelX, paddingTop + chartH - 10f)
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