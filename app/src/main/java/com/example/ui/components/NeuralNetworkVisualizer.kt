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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndigoSecondary
import kotlin.math.min

@Composable
fun NeuralNetworkVisualizer(
    inputFeaturesCount: Int,
    hiddenUnitsCount: Int,
    outputClassesCount: Int,
    isTraining: Boolean,
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
                    text = "Neural Architecture Graph",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$inputFeaturesCount In → $hiddenUnitsCount Hidden → $outputClassesCount Out",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF090D16), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    val inNodes = min(6, inputFeaturesCount)
                    val hidNodes = min(8, hiddenUnitsCount)
                    val outNodes = min(5, outputClassesCount.coerceAtLeast(1))

                    val inX = w * 0.15f
                    val hidX = w * 0.50f
                    val outX = w * 0.85f

                    // Calculate node positions
                    val inPositions = List(inNodes) { idx ->
                        Offset(inX, h * ((idx + 1).toFloat() / (inNodes + 1)))
                    }
                    val hidPositions = List(hidNodes) { idx ->
                        Offset(hidX, h * ((idx + 1).toFloat() / (hidNodes + 1)))
                    }
                    val outPositions = List(outNodes) { idx ->
                        Offset(outX, h * ((idx + 1).toFloat() / (outNodes + 1)))
                    }

                    // Draw synapse connections
                    for (pIn in inPositions) {
                        for (pHid in hidPositions) {
                            drawLine(
                                color = IndigoSecondary.copy(alpha = if (isTraining) 0.35f else 0.18f),
                                start = pIn,
                                end = pHid,
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    for (pHid in hidPositions) {
                        for (pOut in outPositions) {
                            drawLine(
                                color = CyanPrimary.copy(alpha = if (isTraining) 0.40f else 0.20f),
                                start = pHid,
                                end = pOut,
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    // Draw nodes
                    inPositions.forEach { p ->
                        drawCircle(color = IndigoSecondary, radius = 5.dp.toPx(), center = p)
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = p)
                    }

                    hidPositions.forEach { p ->
                        drawCircle(color = if (isTraining) CyanPrimary else Color(0xFF6366F1), radius = 5.5f.dp.toPx(), center = p)
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = p)
                    }

                    outPositions.forEach { p ->
                        drawCircle(color = Color(0xFF10B981), radius = 6.dp.toPx(), center = p)
                        drawCircle(color = Color.White, radius = 2.5f.dp.toPx(), center = p)
                    }
                }
            }
        }
    }
}
