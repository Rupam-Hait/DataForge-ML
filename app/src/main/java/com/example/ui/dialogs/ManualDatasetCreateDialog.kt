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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.data.model.Dataset
import com.example.data.model.DatasetCategory
import com.example.data.model.TaskType
import com.example.ui.theme.CyanPrimary
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualDatasetCreateDialog(
    onDismiss: () -> Unit,
    onSave: (Dataset) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var featureNamesStr by remember { mutableStateOf("feature_1, feature_2, feature_3") }
    var targetColumnName by remember { mutableStateOf("target_label") }
    var classLabelsStr by remember { mutableStateOf("Class A, Class B") }
    var samplesCountStr by remember { mutableStateOf("25") }
    var selectedCategory by remember { mutableStateOf(DatasetCategory.TABULAR) }
    var selectedTaskType by remember { mutableStateOf(TaskType.CLASSIFICATION) }
    var catExpanded by remember { mutableStateOf(false) }
    var taskExpanded by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Dataset",
                        tint = CyanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Custom Dataset",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Dataset Name") },
                placeholder = { Text("e.g. Robot Gripper Pressure Telemetry") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Short Description") },
                placeholder = { Text("Dataset purpose and data collection notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = featureNamesStr,
                onValueChange = { featureNamesStr = it },
                label = { Text("Feature Column Names (Comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetColumnName,
                    onValueChange = { targetColumnName = it },
                    label = { Text("Target Col") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = samplesCountStr,
                    onValueChange = { samplesCountStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Num Samples") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTaskType != TaskType.REGRESSION) {
                OutlinedTextField(
                    value = classLabelsStr,
                    onValueChange = { classLabelsStr = it },
                    label = { Text("Class Labels (Comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = catExpanded,
                onExpandedChange = { catExpanded = !catExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = catExpanded,
                    onDismissRequest = { catExpanded = false }
                ) {
                    DatasetCategory.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                selectedCategory = cat
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task Type Dropdown
            ExposedDropdownMenuBox(
                expanded = taskExpanded,
                onExpandedChange = { taskExpanded = !taskExpanded }
            ) {
                OutlinedTextField(
                    value = selectedTaskType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Task Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = taskExpanded,
                    onDismissRequest = { taskExpanded = false }
                ) {
                    TaskType.values().forEach { task ->
                        DropdownMenuItem(
                            text = { Text(task.displayName) },
                            onClick = {
                                selectedTaskType = task
                                taskExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val features = featureNamesStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val classes = classLabelsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val count = samplesCountStr.toIntOrNull()?.coerceIn(10, 200) ?: 25

                        val rnd = Random(System.currentTimeMillis())
                        val numFeaturesList = mutableListOf<List<Double>>()
                        val numTargetsList = mutableListOf<Double>()
                        val rowsList = mutableListOf<Map<String, String>>()

                        for (i in 0 until count) {
                            val rowVec = features.map { (rnd.nextDouble() * 100.0).let { v -> String.format("%.2f", v).toDouble() } }
                            numFeaturesList.add(rowVec)

                            val rowMap = mutableMapOf<String, String>()
                            features.forEachIndexed { fIdx, fName ->
                                rowMap[fName] = rowVec[fIdx].toString()
                            }

                            if (selectedTaskType == TaskType.REGRESSION) {
                                val targetVal = rowVec.sum() * 0.5 + rnd.nextDouble() * 5.0
                                numTargetsList.add(targetVal)
                                rowMap[targetColumnName] = String.format("%.2f", targetVal)
                            } else {
                                val classIdx = rnd.nextInt(classes.size.coerceAtLeast(2))
                                numTargetsList.add(classIdx.toDouble())
                                rowMap[targetColumnName] = classes.getOrElse(classIdx) { "Class $classIdx" }
                            }
                            rowsList.add(rowMap)
                        }

                        val customDataset = Dataset(
                            id = "ds_user_" + System.currentTimeMillis(),
                            title = title,
                            description = description.ifBlank { "User-defined custom dataset for machine training." },
                            category = selectedCategory,
                            taskType = selectedTaskType,
                            samplesCount = count,
                            featuresCount = features.size,
                            targetColumn = targetColumnName,
                            tags = listOf("Custom Dataset", selectedCategory.displayName),
                            classLabels = classes,
                            featureNames = features,
                            sampleRows = rowsList,
                            numericFeatures = numFeaturesList,
                            numericTargets = numTargetsList,
                            isCustom = true,
                            rating = 5.0,
                            downloadsCount = 1
                        )
                        onSave(customDataset)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF030712))
            ) {
                Text("Create & Add Dataset", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
