package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivationFunction
import com.example.data.model.Dataset
import com.example.data.model.DistanceMetric
import com.example.data.model.Hyperparameters
import com.example.data.model.MLModelType
import com.example.data.model.OptimizerType
import com.example.data.model.PredictionResult
import com.example.data.model.TaskType
import com.example.data.model.TrainingProgressState
import com.example.ui.components.ConfusionMatrixHeatmap
import com.example.ui.components.DecisionBoundaryVisualizer
import com.example.ui.components.FeatureImportanceChart
import com.example.ui.components.LossAccuracyChart
import com.example.ui.components.NeuralNetworkVisualizer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelTrainerScreen(
    dataset: Dataset,
    hyperparams: Hyperparameters,
    trainingProgress: TrainingProgressState,
    predictionInputs: Map<String, Double>,
    predictionResult: PredictionResult?,
    onHyperparamsChange: ((Hyperparameters) -> Hyperparameters) -> Unit,
    onStartTraining: () -> Unit,
    onPredictionInputChange: (String, Double) -> Unit,
    onExportCodeClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    val isClassification = dataset.taskType != TaskType.REGRESSION

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Training Studio: ${dataset.title.take(20)}...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onExportCodeClick) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = "Export Code", tint = CyanPrimary)
                }
            }
        }

        // Model Architecture Selection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Algorithm & Machine Learning Class",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = modelDropdownExpanded,
                        onExpandedChange = { modelDropdownExpanded = !modelDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = hyperparams.modelType.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selected Model Architecture") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = modelDropdownExpanded,
                            onDismissRequest = { modelDropdownExpanded = false }
                        ) {
                            MLModelType.values().forEach { modelType ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(modelType.title, fontWeight = FontWeight.Bold)
                                            Text(modelType.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        onHyperparamsChange { it.copy(modelType = modelType) }
                                        modelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hyperparams.modelType.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Neural Visualizer (if MLP)
        if (hyperparams.modelType == MLModelType.NEURAL_NETWORK_MLP) {
            item {
                NeuralNetworkVisualizer(
                    inputFeaturesCount = dataset.featuresCount,
                    hiddenUnitsCount = hyperparams.hiddenUnits,
                    outputClassesCount = dataset.classLabels.size,
                    isTraining = trainingProgress.isTraining
                )
            }
        }

        // 2D Continuous Decision Boundary Spatial Visualizer (if Classification)
        if (isClassification && dataset.featuresCount >= 2) {
            item {
                DecisionBoundaryVisualizer(
                    boundaryGrid = trainingProgress.boundaryGrid,
                    rawPoints = dataset.numericFeatures,
                    targets = dataset.numericTargets,
                    feature1Name = dataset.featureNames.getOrElse(0) { "F1" },
                    feature2Name = dataset.featureNames.getOrElse(1) { "F2" },
                    classLabels = dataset.classLabels
                )
            }
        }

        // Hyperparameter Tuning Controls
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hyperparameters & Optimization",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Epochs Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Epochs (Iterations)", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${hyperparams.epochs}", fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }
                    Slider(
                        value = hyperparams.epochs.toFloat(),
                        onValueChange = { onHyperparamsChange { p -> p.copy(epochs = it.roundToInt()) } },
                        valueRange = 10f..100f,
                        steps = 17,
                        colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                    )

                    // Learning Rate Slider (for gradient-based models)
                    if (hyperparams.modelType in listOf(MLModelType.NEURAL_NETWORK_MLP, MLModelType.LOGISTIC_REGRESSION, MLModelType.LINEAR_REGRESSION)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Learning Rate (α)", style = MaterialTheme.typography.bodyMedium)
                            Text(text = String.format("%.3f", hyperparams.learningRate), fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Slider(
                            value = hyperparams.learningRate.toFloat(),
                            onValueChange = { onHyperparamsChange { p -> p.copy(learningRate = it.toDouble()) } },
                            valueRange = 0.005f..0.20f,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )
                    }

                    // Optimizer Selector (for MLP)
                    if (hyperparams.modelType == MLModelType.NEURAL_NETWORK_MLP) {
                        Text(text = "Optimizer Algorithm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OptimizerType.values().forEach { opt ->
                                FilterChip(
                                    selected = hyperparams.optimizer == opt,
                                    onClick = { onHyperparamsChange { it.copy(optimizer = opt) } },
                                    label = { Text(opt.displayName.take(12), fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary,
                                        selectedLabelColor = Color(0xFF030712)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // KNN K-Neighbors Slider
                    if (hyperparams.modelType == MLModelType.K_NEAREST_NEIGHBORS) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "K Nearest Neighbors", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${hyperparams.kNeighbors}", fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Slider(
                            value = hyperparams.kNeighbors.toFloat(),
                            onValueChange = { onHyperparamsChange { p -> p.copy(kNeighbors = it.roundToInt()) } },
                            valueRange = 1f..15f,
                            steps = 13,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )
                    }

                    // Decision Tree / Random Forest Max Depth Slider
                    if (hyperparams.modelType in listOf(MLModelType.DECISION_TREE, MLModelType.RANDOM_FOREST)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Max Tree Depth", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${hyperparams.maxTreeDepth}", fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Slider(
                            value = hyperparams.maxTreeDepth.toFloat(),
                            onValueChange = { onHyperparamsChange { p -> p.copy(maxTreeDepth = it.roundToInt()) } },
                            valueRange = 2f..8f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )
                    }

                    // Random Forest Trees Count
                    if (hyperparams.modelType == MLModelType.RANDOM_FOREST) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Ensemble Estimators (Trees)", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${hyperparams.nTrees}", fontWeight = FontWeight.Bold, color = IndigoSecondary)
                        }
                        Slider(
                            value = hyperparams.nTrees.toFloat(),
                            onValueChange = { onHyperparamsChange { p -> p.copy(nTrees = it.roundToInt()) } },
                            valueRange = 5f..35f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = IndigoSecondary, activeTrackColor = IndigoSecondary)
                        )
                    }

                    // Test Split Ratio Slider
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Validation Split Ratio", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${(hyperparams.testSplitRatio * 100).toInt()}%", fontWeight = FontWeight.Bold, color = IndigoSecondary)
                    }
                    Slider(
                        value = hyperparams.testSplitRatio,
                        onValueChange = { onHyperparamsChange { p -> p.copy(testSplitRatio = it) } },
                        valueRange = 0.10f..0.40f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = IndigoSecondary, activeTrackColor = IndigoSecondary)
                    )
                }
            }
        }

        // Primary Train Action Button
        item {
            Button(
                onClick = onStartTraining,
                enabled = !trainingProgress.isTraining,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF030712))
            ) {
                if (trainingProgress.isTraining) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color(0xFF030712), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Training Epoch ${trainingProgress.currentEpoch}/${trainingProgress.totalEpochs}...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (trainingProgress.isCompleted) "Re-train Model" else "Execute Training Loop",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Live Training Progress Bar
        if (trainingProgress.isTraining) {
            item {
                LinearProgressIndicator(
                    progress = { (trainingProgress.currentEpoch.toFloat() / trainingProgress.totalEpochs.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = CyanPrimary,
                    trackColor = Color(0xFF1E293B)
                )
            }
        }

        // Live Loss & Accuracy Chart
        item {
            LossAccuracyChart(
                metricsHistory = trainingProgress.metricsHistory,
                totalEpochs = hyperparams.epochs,
                isClassification = isClassification
            )
        }

        // Model Performance Summary Metrics
        if (trainingProgress.isCompleted) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Training Results & Convergence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = trainingProgress.modelSummary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (isClassification) {
                                MetricResultBox(label = "Validation Accuracy", value = "${String.format("%.1f", trainingProgress.currentValAcc * 100)}%", color = EmeraldSuccess, modifier = Modifier.weight(1f))
                                MetricResultBox(label = "Validation Loss", value = String.format("%.4f", trainingProgress.currentValLoss), color = CyanPrimary, modifier = Modifier.weight(1f))
                            } else {
                                MetricResultBox(label = "R² Fit Score", value = String.format("%.3f", trainingProgress.r2Score), color = EmeraldSuccess, modifier = Modifier.weight(1f))
                                MetricResultBox(label = "Mean Squared Error", value = String.format("%.4f", trainingProgress.mseScore), color = CyanPrimary, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Feature Importance Chart
            if (trainingProgress.featureImportances.isNotEmpty()) {
                item {
                    FeatureImportanceChart(importances = trainingProgress.featureImportances)
                }
            }

            // Tree Split Rules Inspector (if Decision Tree)
            if (hyperparams.modelType == MLModelType.DECISION_TREE && trainingProgress.treeRules.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AccountTree, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Decision Tree Split Logic", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            trainingProgress.treeRules.take(5).forEachIndexed { idx, rule ->
                                Text(
                                    text = "Rule ${idx + 1}: IF ${rule.featureName} <= ${String.format("%.2f", rule.threshold)} THEN '${rule.leftLabel}' ELSE '${rule.rightLabel}'",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Confusion Matrix
            if (isClassification && trainingProgress.confusionMatrix.isNotEmpty()) {
                item {
                    ConfusionMatrixHeatmap(
                        matrix = trainingProgress.confusionMatrix,
                        classLabels = dataset.classLabels
                    )
                }
            }
        }

        // Interactive Prediction Sandbox
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interactive Prediction Sandbox",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Adjust feature inputs below to run real-time inference on the trained model weights.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feature sliders
                    dataset.featureNames.forEach { featureName ->
                        val currentVal = predictionInputs[featureName] ?: 1.0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = featureName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text(text = String.format("%.2f", currentVal), fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Slider(
                            value = currentVal.toFloat(),
                            onValueChange = { onPredictionInputChange(featureName, it.toDouble()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Prediction Output Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090D16), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        if (predictionResult == null) {
                            Text(
                                text = "Train the model or tweak input sliders above to compute real-time prediction output.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        } else {
                            Column {
                                Text(
                                    text = "Inference Result:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isClassification) predictionResult.predictedClass else String.format("%.3f", predictionResult.predictedContinuousValue),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldSuccess
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Surface(
                                        color = EmeraldSuccess.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Confidence: ${(predictionResult.confidence * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSuccess,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (predictionResult.nearestNeighborsInfo.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Top Nearest Neighbors:", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                                    predictionResult.nearestNeighborsInfo.take(3).forEach { nInfo ->
                                        Text(text = nInfo, fontSize = 10.sp, color = Color.LightGray)
                                    }
                                }

                                if (predictionResult.classProbabilities.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    predictionResult.classProbabilities.forEach { (label, prob) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = label, modifier = Modifier.width(90.dp), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color.LightGray)
                                            LinearProgressIndicator(
                                                progress = { prob.toFloat().coerceIn(0f, 1f) },
                                                modifier = Modifier.weight(1f).height(6.dp),
                                                color = CyanPrimary,
                                                trackColor = Color(0xFF1E293B)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "${(prob * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricResultBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF090D16),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

