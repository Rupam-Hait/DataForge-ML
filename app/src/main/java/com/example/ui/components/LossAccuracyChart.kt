package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EpochMetric
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.RoseError
import kotlin.math.max

@Composable
fun LossAccuracyChart(
    metricsHistory: List<EpochMetric>,
    totalEpochs: Int,
    isClassification: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Convergence Curves",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Epochs: ${metricsHistory.size}/$totalEpochs",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem(color = RoseError, label = "Train Loss")
                LegendItem(color = AmberColor, label = "Val Loss")
                if (isClassification) {
                    LegendItem(color = EmeraldSuccess, label = "Train Acc")
                    LegendItem(color = CyanPrimary, label = "Val Acc")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF090D16), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (metricsHistory.isEmpty()) {
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        Text(
                            text = "Waiting for training to start...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val width = size.width
                        val height = size.height

                        // Grid lines
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val y = height * (i.toFloat() / gridLines)
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val maxLoss = max(1.0, metricsHistory.maxOfOrNull { max(it.trainLoss, it.valLoss) } ?: 1.0)
                        val pointsCount = max(2, totalEpochs)

                        // 1. Draw Train Loss (Rose)
                        val trainLossPath = Path()
                        metricsHistory.forEachIndexed { idx, m ->
                            val x = (idx.toFloat() / (pointsCount - 1)) * width
                            val y = height - (m.trainLoss.toFloat() / maxLoss.toFloat()) * height
                            if (idx == 0) trainLossPath.moveTo(x, y.coerceIn(0f, height))
                            else trainLossPath.lineTo(x, y.coerceIn(0f, height))
                        }
                        drawPath(trainLossPath, RoseError, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                        // 2. Draw Val Loss (Amber)
                        val valLossPath = Path()
                        metricsHistory.forEachIndexed { idx, m ->
                            val x = (idx.toFloat() / (pointsCount - 1)) * width
                            val y = height - (m.valLoss.toFloat() / maxLoss.toFloat()) * height
                            if (idx == 0) valLossPath.moveTo(x, y.coerceIn(0f, height))
                            else valLossPath.lineTo(x, y.coerceIn(0f, height))
                        }
                        drawPath(valLossPath, AmberColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                        // 3. Draw Accuracy if classification (Emerald / Cyan)
                        if (isClassification) {
                            val trainAccPath = Path()
                            metricsHistory.forEachIndexed { idx, m ->
                                val x = (idx.toFloat() / (pointsCount - 1)) * width
                                val y = height - (m.trainAccuracy.toFloat() * height)
                                if (idx == 0) trainAccPath.moveTo(x, y.coerceIn(0f, height))
                                else trainAccPath.lineTo(x, y.coerceIn(0f, height))
                            }
                            drawPath(trainAccPath, EmeraldSuccess, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                            val valAccPath = Path()
                            metricsHistory.forEachIndexed { idx, m ->
                                val x = (idx.toFloat() / (pointsCount - 1)) * width
                                val y = height - (m.valAccuracy.toFloat() * height)
                                if (idx == 0) valAccPath.moveTo(x, y.coerceIn(0f, height))
                                else valAccPath.lineTo(x, y.coerceIn(0f, height))
                            }
                            drawPath(valAccPath, CyanPrimary, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                }
            }
        }
    }
}

private val AmberColor = Color(0xFFF59E0B)

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
