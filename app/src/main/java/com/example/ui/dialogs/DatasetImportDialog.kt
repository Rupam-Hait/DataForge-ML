package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dataset
import com.example.data.model.DatasetCategory
import com.example.data.model.TaskType
import com.example.ml.engine.MLEngine
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndigoSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetImportDialog(
    onDismiss: () -> Unit,
    onDatasetImported: (Dataset) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var rawCsvText by remember {
        mutableStateOf(
            """sepal_length,sepal_width,petal_length,petal_width,species
5.1,3.5,1.4,0.2,setosa
4.9,3.0,1.4,0.2,setosa
4.7,3.2,1.3,0.2,setosa
7.0,3.2,4.7,1.4,versicolor
6.4,3.2,4.5,1.5,versicolor
6.9,3.1,4.9,1.5,versicolor
6.3,3.3,6.0,2.5,virginica
5.8,2.7,5.1,1.9,virginica
7.1,3.0,5.9,2.1,virginica
6.3,2.9,5.6,1.8,virginica"""
        )
    }

    val samplePresets = listOf(
        "Iris Botanical" to """sepal_length,sepal_width,petal_length,petal_width,species
5.1,3.5,1.4,0.2,setosa
4.9,3.0,1.4,0.2,setosa
7.0,3.2,4.7,1.4,versicolor
6.4,3.2,4.5,1.5,versicolor
6.3,3.3,6.0,2.5,virginica
5.8,2.7,5.1,1.9,virginica""",
        "Telemetry IoT" to """voltage,temperature,vibration,pressure,anomaly
12.4,45.2,0.12,101.3,0
12.1,48.0,0.15,102.1,0
13.8,78.5,0.88,118.4,1
11.9,44.1,0.11,100.9,0
14.2,82.1,0.95,122.0,1"""
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import CSV / Data Pipeline",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "Paste comma-separated (CSV) records. The parser automatically detects feature dimensions, data types, and target classification labels.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Dataset Title") },
                placeholder = { Text("e.g. Sensor Array Telemetry") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                samplePresets.forEach { (name, csv) ->
                    Surface(
                        onClick = {
                            title = name
                            rawCsvText = csv
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = IndigoSecondary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Load $name",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = rawCsvText,
                onValueChange = { rawCsvText = it },
                label = { Text("Raw CSV Content (Headers in 1st row)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val dataset = parseCsvToDataset(title.ifBlank { "Imported Custom Dataset" }, rawCsvText)
                    if (dataset != null) {
                        onDatasetImported(dataset)
                    }
                },
                enabled = rawCsvText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF030712))
            ) {
                Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Parse & Ingest Dataset", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun parseCsvToDataset(title: String, csvText: String): Dataset? {
    val lines = csvText.trim().lines().map { it.trim() }.filter { it.isNotBlank() }
    if (lines.size < 2) return null

    val headers = lines[0].split(",").map { it.trim() }
    val targetColumn = headers.last()
    val featureNames = headers.dropLast(1)

    val rows = mutableListOf<Map<String, String>>()
    val numericFeatures = mutableListOf<List<Double>>()
    val numericTargets = mutableListOf<Double>()
    val classLabels = mutableListOf<String>()

    for (i in 1 until lines.size) {
        val parts = lines[i].split(",").map { it.trim() }
        if (parts.size >= headers.size) {
            val rowMap = mutableMapOf<String, String>()
            headers.forEachIndexed { idx, h -> rowMap[h] = parts[idx] }
            rows.add(rowMap)

            val featVector = featureNames.map { h ->
                rowMap[h]?.toDoubleOrNull() ?: 0.0
            }
            numericFeatures.add(featVector)

            val targetValStr = rowMap[targetColumn] ?: "0"
            val targetDouble = targetValStr.toDoubleOrNull()
            if (targetDouble != null) {
                numericTargets.add(targetDouble)
                val label = "Class ${targetDouble.toInt()}"
                if (!classLabels.contains(label)) classLabels.add(label)
            } else {
                if (!classLabels.contains(targetValStr)) classLabels.add(targetValStr)
                numericTargets.add(classLabels.indexOf(targetValStr).toDouble())
            }
        }
    }

    val dataset = Dataset(
        id = "ds_imported_" + System.currentTimeMillis(),
        title = title,
        description = "User-imported CSV dataset with ${headers.size} dimensions.",
        category = DatasetCategory.TABULAR,
        taskType = TaskType.CLASSIFICATION,
        samplesCount = rows.size,
        featuresCount = featureNames.size,
        targetColumn = targetColumn,
        tags = listOf("CSV Import", "Custom"),
        featureNames = featureNames,
        classLabels = if (classLabels.isEmpty()) listOf("Class 0", "Class 1") else classLabels,
        sampleRows = rows,
        numericFeatures = numericFeatures,
        numericTargets = numericTargets,
        isCustom = true,
        rating = 5.0,
        downloadsCount = 1
    )

    val correlations = MLEngine.computePearsonCorrelations(dataset)
    val stats = MLEngine.computeColumnStats(dataset)
    return dataset.copy(correlations = correlations, columnStats = stats)
}
