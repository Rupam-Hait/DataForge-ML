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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColumnStat
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureDistributionCard(
    columnStats: List<ColumnStat>,
    modifier: Modifier = Modifier
) {
    if (columnStats.isEmpty()) return

    var selectedIndex by remember { mutableStateOf(0) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val currentStat = columnStats.getOrElse(selectedIndex) { columnStats[0] }

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
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Feature Distribution & Density",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feature Dropdown
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = currentStat.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Inspect Feature") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    columnStats.forEachIndexed { idx, stat ->
                        DropdownMenuItem(
                            text = { Text(stat.name, fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                selectedIndex = idx
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Histogram Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF070B14), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (currentStat.histogramBins.isEmpty()) {
                    Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Distribution binning loaded", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                } else {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val bins = currentStat.histogramBins
                        val maxCount = bins.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
                        val barSpacing = 4f
                        val barWidth = (canvasW - (bins.size - 1) * barSpacing) / bins.size

                        bins.forEachIndexed { i, bin ->
                            val barHeight = (bin.second.toFloat() / maxCount) * (canvasH - 16f)
                            val x = i * (barWidth + barSpacing)
                            val y = canvasH - barHeight

                            drawRect(
                                color = CyanPrimary.copy(alpha = 0.85f),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Statistical Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge("Mean", String.format("%.2f", currentStat.mean), Modifier.weight(1f))
                StatBadge("Median", String.format("%.2f", currentStat.median), Modifier.weight(1f))
                StatBadge("Std Dev", String.format("%.2f", currentStat.stdDev), Modifier.weight(1f))
                StatBadge("Min / Max", "${String.format("%.1f", currentStat.min)} / ${String.format("%.1f", currentStat.max)}", Modifier.weight(1.2f))
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF0D1527),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyanPrimary, maxLines = 1)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.Gray)
        }
    }
}
