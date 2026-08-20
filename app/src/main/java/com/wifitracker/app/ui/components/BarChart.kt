package com.wifitracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RoundRect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifitracker.app.data.PeriodStat
import com.wifitracker.app.ui.theme.AccentBlue
import com.wifitracker.app.ui.theme.AccentCyan

@Composable
fun WifiBarChart(
    bars: List<PeriodStat>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barHeight: androidx.compose.ui.unit.Dp = 140.dp
) {
    val maxDuration = (bars.maxOfOrNull { it.durationMillis } ?: 1L).coerceAtLeast(1L)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight + 28.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
    ) {
        bars.forEachIndexed { index, stat ->
            val fraction = (stat.durationMillis.toFloat() / maxDuration.toFloat()).coerceIn(0f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 3.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .height(barHeight)
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                ) {
                    val barWidth = size.width.coerceAtMost(28.dp.toPx())
                    val left = (size.width - barWidth) / 2
                    val fullHeight = size.height
                    val filledHeight = fullHeight * fraction
                    val top = fullHeight - filledHeight

                    // ব্যাকগ্রাউন্ড ট্র্যাক
                    drawRoundRect(
                        color = androidx.compose.ui.graphics.Color(0xFF23262D),
                        topLeft = androidx.compose.ui.geometry.Offset(left, 0f),
                        size = androidx.compose.ui.geometry.Size(barWidth, fullHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )

                    if (filledHeight > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(AccentCyan, AccentBlue)),
                            topLeft = androidx.compose.ui.geometry.Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(barWidth, filledHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )
                    }
                }
                if (index < labels.size) {
                    Text(
                        text = labels[index],
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}
