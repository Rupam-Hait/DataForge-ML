package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.Hyperparameters
import com.example.ml.export.CodeExportGenerator
import com.example.ui.components.CodeSnippetBox
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndigoSecondary

enum class ExportLanguage(val displayName: String, val icon: String) {
    PYTORCH("PyTorch 2.x", "python"),
    TENSORFLOW("TensorFlow / Keras", "python"),
    SCIKIT_LEARN("Scikit-Learn Pipeline", "python"),
    FASTAPI_SERVER("FastAPI Server (REST)", "api"),
    ONNX_RUNTIME("ONNX Runtime Engine", "onnx"),
    CSV("Raw CSV Data", "table"),
    JSON("JSON Dataset", "json")
}

@Composable
fun CodeExportScreen(
    dataset: Dataset,
    hyperparams: Hyperparameters,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLanguage by remember { mutableStateOf(ExportLanguage.PYTORCH) }

    val codeText = remember(dataset, hyperparams, selectedLanguage) {
        when (selectedLanguage) {
            ExportLanguage.PYTORCH -> CodeExportGenerator.generatePyTorch(dataset, hyperparams)
            ExportLanguage.TENSORFLOW -> CodeExportGenerator.generateTensorFlow(dataset, hyperparams)
            ExportLanguage.SCIKIT_LEARN -> CodeExportGenerator.generateScikitLearn(dataset, hyperparams)
            ExportLanguage.FASTAPI_SERVER -> CodeExportGenerator.generateFastApiServer(dataset)
            ExportLanguage.ONNX_RUNTIME -> CodeExportGenerator.generateOnnxExport(dataset, hyperparams)
            ExportLanguage.CSV -> CodeExportGenerator.generateCsv(dataset)
            ExportLanguage.JSON -> CodeExportGenerator.generateJson(dataset)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
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
                    text = "Code & Data Pipeline Exporter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Info Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Production Machine Training Scripts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Export fully-executable training pipelines for PyTorch, TensorFlow/Keras, and Scikit-Learn pre-configured with '${dataset.title}' feature tensors and data loaders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Framework Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportLanguage.values().forEach { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { selectedLanguage = lang },
                        label = { Text(lang.displayName, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = Color(0xFF030712)
                        )
                    )
                }
            }
        }

        // Code Snippet Box
        item {
            CodeSnippetBox(
                codeText = codeText,
                languageTitle = "${selectedLanguage.displayName} • ${dataset.title}"
            )
        }

        // How to Run Guide
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Execution Instructions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Click the Copy button in the top-right to copy script to clipboard.\n2. Paste into Google Colab, Jupyter Notebook, or local `train.py`.\n3. Run `python train.py` to train model and save weights artifact.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
