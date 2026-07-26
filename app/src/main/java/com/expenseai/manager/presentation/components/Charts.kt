package com.expenseai.manager.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expenseai.manager.ui.theme.chartColors
import kotlin.math.*

data class PieSlice(val label: String, val value: Double, val color: Color)
data class BarData(val label: String, val value: Double, val color: Color = Color.Unspecified)
data class LinePoint(val x: Float, val y: Float, val label: String = "")

@Composable
fun AnimatedPieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    donut: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutQuart),
        label = "pie"
    )

    val total = slices.sumOf { it.value }
    if (total <= 0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val onSurface = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier.aspectRatio(1f)) {
        var startAngle = -90f
        val diameter = size.minDimension
        val radius = diameter / 2
        val strokeWidth = if (donut) diameter * 0.20f else 0f
        val rect = Rect(
            offset = Offset((size.width - diameter) / 2, (size.height - diameter) / 2),
            size = Size(diameter, diameter)
        )

        slices.forEach { slice ->
            val sweepAngle = ((slice.value / total) * 360f * animatedProgress).toFloat()
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 1f,
                useCenter = !donut,
                topLeft = rect.topLeft,
                size = Size(diameter, diameter),
                style = if (donut) Stroke(width = strokeWidth, cap = StrokeCap.Butt) else Fill
            )
            startAngle += sweepAngle
        }

        if (donut) {
            drawCircle(
                color = onSurface,
                radius = radius - strokeWidth,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }
}

@Composable
fun PieChartLegend(slices: List<PieSlice>, total: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        slices.sortedByDescending { it.value }.take(6).forEach { slice ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(slice.color)
                )
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = if (total > 0) "${((slice.value / total) * 100).toInt()}%" else "0%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    maxValue: Double? = null,
    showLabels: Boolean = true,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "bar"
    )

    val max = (maxValue ?: data.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0)
    val defaultColor = barColor
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val barWidth = (size.width - 32.dp.toPx()) / data.size
            val chartHeight = size.height - if (showLabels) 24.dp.toPx() else 0f
            val cornerRadius = 6.dp.toPx()

            data.forEachIndexed { index, barData ->
                val barHeight = ((barData.value / max) * chartHeight * animatedProgress).toFloat()
                val x = index * barWidth + barWidth * 0.1f
                val w = barWidth * 0.8f
                val y = chartHeight - barHeight

                val color = if (barData.color == Color.Unspecified) defaultColor else barData.color
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(w, barHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
        }

        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                data.forEach { barData ->
                    Text(
                        text = barData.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(
    points: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillArea: Boolean = true,
    showDots: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "line"
    )

    if (points.isEmpty()) return

    val maxValue = points.maxOf { it.second }.coerceAtLeast(1.0)
    val minValue = points.minOf { it.second }.coerceAtMost(maxValue - 1.0)
    val range = (maxValue - minValue).coerceAtLeast(1.0)

    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier) {
        val paddingHorizontal = 24.dp.toPx()
        val paddingVertical = 16.dp.toPx()
        val chartWidth = size.width - paddingHorizontal * 2
        val chartHeight = size.height - paddingVertical * 2

        fun xOf(index: Int) = paddingHorizontal + (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * chartWidth
        fun yOf(value: Double) = paddingVertical + ((maxValue - value) / range).toFloat() * chartHeight

        val animatedPointCount = (points.size * animatedProgress).toInt().coerceAtLeast(1)
        val visiblePoints = points.take(animatedPointCount)

        if (fillArea && visiblePoints.size > 1) {
            val path = Path()
            path.moveTo(xOf(0), yOf(visiblePoints.first().second))
            for (i in 1 until visiblePoints.size) {
                val x1 = xOf(i - 1)
                val x2 = xOf(i)
                val cp1X = x1 + (x2 - x1) / 3
                val cp2X = x1 + 2 * (x2 - x1) / 3
                path.cubicTo(cp1X, yOf(visiblePoints[i-1].second), cp2X, yOf(visiblePoints[i].second), x2, yOf(visiblePoints[i].second))
            }
            path.lineTo(xOf(visiblePoints.size - 1), size.height - paddingVertical)
            path.lineTo(xOf(0), size.height - paddingVertical)
            path.close()

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
                )
            )
        }

        if (visiblePoints.size > 1) {
            val linePath = Path()
            linePath.moveTo(xOf(0), yOf(visiblePoints.first().second))
            for (i in 1 until visiblePoints.size) {
                val x1 = xOf(i - 1)
                val x2 = xOf(i)
                val cp1X = x1 + (x2 - x1) / 3
                val cp2X = x1 + 2 * (x2 - x1) / 3
                linePath.cubicTo(cp1X, yOf(visiblePoints[i-1].second), cp2X, yOf(visiblePoints[i].second), x2, yOf(visiblePoints[i].second))
            }
            drawPath(path = linePath, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }

        if (showDots) {
            visiblePoints.forEachIndexed { i, point ->
                drawCircle(color = surfaceColor, radius = 6.dp.toPx(), center = Offset(xOf(i), yOf(point.second)))
                drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(xOf(i), yOf(point.second)))
            }
        }
    }
}

@Composable
fun BudgetProgressBar(
    spent: Double,
    budget: Double,
    currency: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val percentage = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0) else 0.0
    val animatedProgress by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "budget"
    )

    val color = when {
        percentage >= 1.0 -> MaterialTheme.colorScheme.error
        percentage >= 0.80 -> Color(0xFFFF8F00)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${com.expenseai.manager.util.CurrencyUtils.format(spent, currency)} spent",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "of ${com.expenseai.manager.util.CurrencyUtils.format(budget, currency)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
