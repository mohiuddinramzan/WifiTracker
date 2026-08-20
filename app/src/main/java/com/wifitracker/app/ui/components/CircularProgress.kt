package com.wifitracker.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifitracker.app.ui.theme.AccentBlue
import com.wifitracker.app.ui.theme.AccentCyan
import com.wifitracker.app.ui.theme.SurfaceVariantDark

@Composable
fun WifiRingProgress(
    progress: Float,
    centerLine1: String,
    centerLine2: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 220.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 18.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "ring_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2
            val arcSize = Size(size.toPx() - strokeWidth.toPx(), size.toPx() - strokeWidth.toPx())

            drawArc(
                color = SurfaceVariantDark,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = stroke
            )

            drawArc(
                brush = Brush.sweepGradient(listOf(AccentBlue, AccentCyan, AccentBlue)),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
        }

        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = centerLine1,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = centerLine2,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
