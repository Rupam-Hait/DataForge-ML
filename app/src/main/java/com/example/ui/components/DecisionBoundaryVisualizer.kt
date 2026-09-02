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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DecisionBoundaryPoint
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.RoseError

private val classColors = listOf(
    Color(0xFF00E5FF), // Cyan
    Color(0xFF818CF8), // Indigo
    Color(0xFF10B981), // Emerald
    Color(0xFFF43F5E), // Rose
    Color(0xFFFBBF24), // Amber
    Color(0xFFA855F7)  // Purple
)

@Composable
fun DecisionBoundaryVisualizer(
    boundaryGrid: List<DecisionBoundaryPoint>,
    rawPoints: List<List<Double>>,
    targets: List<Double>,
    feature1Name: String,
    feature2Name: String,
    classLabels: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2D Decision Boundary Space",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = IndigoSecondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$feature1Name vs $feature2Name",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Continuous spatial classification landscape and partition surfaces evaluated across the normalized 2D feature domain.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color(0xFF070B14), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (boundaryGrid.isEmpty()) {
                    Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Train the model to render spatial decision boundaries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val minCoord = -2.5f
                        val maxCoord = 2.5f
                        val range = maxCoord - minCoord

                        // 1. Draw Decision Boundary Shaded Mesh Grid
                        val res = 16
                        val cellW = canvasW / res
                        val cellH = canvasH / res

                        for (pt in boundaryGrid) {
                            val normX = ((pt.x.toFloat() - minCoord) / range).coerceIn(0f, 1f)
                            val normY = (1f - (pt.y.toFloat() - minCoord) / range).coerceIn(0f, 1f)
                            val col = classColors.getOrElse(pt.predictedClass) { CyanPrimary }
                            val alpha = (pt.confidence * 0.35f).toFloat().coerceIn(0.12f, 0.45f)

                            drawRect(
                                color = col.copy(alpha = alpha),
                                topLeft = Offset(normX * (canvasW - cellW), normY * (canvasH - cellH)),
                                size = Size(cellW + 1f, cellH + 1f)
                            )
                        }

                        // 2. Draw Coordinate Crosshairs
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(0f, canvasH / 2f),
                            end = Offset(canvasW, canvasH / 2f),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(canvasW / 2f, 0f),
                            end = Offset(canvasW / 2f, canvasH),
                            strokeWidth = 1f
                        )

                        // 3. Draw Training Scatter Points
                        val numPts = rawPoints.size
                        if (numPts > 0) {
                            val f1Mean = rawPoints.map { it.getOrElse(0) { 0.0 } }.average()
                            val f1Std = (rawPoints.map { kotlin.math.abs(it.getOrElse(0) { 0.0 } - f1Mean) }.average()).coerceAtLeast(0.01)
                            val f2Mean = rawPoints.map { it.getOrElse(1) { 0.0 } }.average()
                            val f2Std = (rawPoints.map { kotlin.math.abs(it.getOrElse(1) { 0.0 } - f2Mean) }.average()).coerceAtLeast(0.01)

                            for (i in 0 until minOf(numPts, 120)) {
                                val v1 = rawPoints[i].getOrElse(0) { 0.0 }
                                val v2 = rawPoints[i].getOrElse(1) { 0.0 }
                                val normX = (((v1 - f1Mean) / f1Std).toFloat() - minCoord) / range
                                val normY = 1f - (((v2 - f2Mean) / f2Std).toFloat() - minCoord) / range
                                val targetClass = targets.getOrElse(i) { 0.0 }.toInt()
                                val ptColor = classColors.getOrElse(targetClass) { CyanPrimary }

                                val px = (normX * canvasW).coerceIn(4f, canvasW - 4f)
                                val py = (normY * canvasH).coerceIn(4f, canvasH - 4f)

                                drawCircle(
                                    color = Color.Black,
                                    radius = 5.5f,
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = ptColor,
                                    radius = 4f,
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Class Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                classLabels.take(4).forEachIndexed { idx, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(classColors.getOrElse(idx) { CyanPrimary }, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
