package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dataset
import com.example.ui.theme.CyanPrimary

@Composable
fun DatasetPreviewTable(
    dataset: Dataset,
    modifier: Modifier = Modifier
) {
    if (dataset.sampleRows.isEmpty()) {
        Text(
            text = "No sample rows available for preview.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val columns = dataset.sampleRows[0].keys.toList()
    val scrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dataset Records Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${dataset.sampleRows.size} rows / ${columns.size} cols",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Data Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            modifier = Modifier.width(36.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        columns.forEach { colName ->
                            val isTarget = colName.equals(dataset.targetColumn, ignoreCase = true)
                            Text(
                                text = if (isTarget) "★ $colName" else colName,
                                modifier = Modifier.width(130.dp).padding(horizontal = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isTarget) CyanPrimary else Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Data Rows (Showing up to 10 sample records)
                    dataset.sampleRows.take(10).forEachIndexed { index, row ->
                        val isEven = index % 2 == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isEven) Color(0xFF1E293B).copy(alpha = 0.4f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                modifier = Modifier.width(36.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            columns.forEach { colName ->
                                val cellVal = row[colName] ?: "-"
                                val isTarget = colName.equals(dataset.targetColumn, ignoreCase = true)
                                Text(
                                    text = cellVal,
                                    modifier = Modifier.width(130.dp).padding(horizontal = 4.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (isTarget) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isTarget) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
