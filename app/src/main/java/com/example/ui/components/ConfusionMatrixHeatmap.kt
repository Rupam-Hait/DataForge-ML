package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import kotlin.math.max

@Composable
fun ConfusionMatrixHeatmap(
    matrix: List<List<Int>>,
    classLabels: List<String>,
    modifier: Modifier = Modifier
) {
    if (matrix.isEmpty()) return

    val numClasses = matrix.size
    val totalSamples = matrix.flatten().sum().coerceAtLeast(1)
    val maxCellVal = matrix.flatten().maxOrNull()?.coerceAtLeast(1) ?: 1

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Confusion Matrix Heatmap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Rows: Actual ground truth | Columns: Model prediction",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Column Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.width(64.dp))
                for (c in 0 until numClasses) {
                    val label = classLabels.getOrElse(c) { "C$c" }
                    Text(
                        text = "Pred $label",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rows
            for (r in 0 until numClasses) {
                val rowLabel = classLabels.getOrElse(r) { "C$r" }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rowLabel,
                        modifier = Modifier.width(64.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    for (c in 0 until numClasses) {
                        val count = matrix[r].getOrElse(c) { 0 }
                        val ratio = count.toFloat() / maxCellVal
                        val isDiagonal = r == c
                        val cellColor = if (isDiagonal) {
                            EmeraldSuccess.copy(alpha = 0.2f + ratio * 0.7f)
                        } else {
                            if (count > 0) Color(0xFFF43F5E).copy(alpha = 0.2f + ratio * 0.6f)
                            else Color(0xFF1E293B).copy(alpha = 0.4f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .padding(horizontal = 2.dp)
                                .background(cellColor, RoundedCornerShape(6.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isDiagonal) EmeraldSuccess.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isDiagonal) FontWeight.Bold else FontWeight.Normal,
                                color = if (count > 0) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
