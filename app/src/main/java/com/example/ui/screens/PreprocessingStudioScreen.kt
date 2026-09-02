package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.OutlierStrategy
import com.example.data.model.PreprocessingConfig
import com.example.data.model.ScalerType
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary

@Composable
fun PreprocessingStudioScreen(
    dataset: Dataset,
    config: PreprocessingConfig,
    onConfigChange: (PreprocessingConfig) -> Unit,
    onProceedToTrain: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "Feature Engineering & Preprocessing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Transform,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pipeline Transformation Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Clean, scale, and synthetically augment raw features to prevent vanishing gradients, eliminate scale bias, and maximize model accuracy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Feature Scaling Strategy Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Normalization & Feature Scaling",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current Formula: ${config.scalerType.formula}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ScalerType.values().forEach { scaler ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scaler.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (config.scalerType == scaler) FontWeight.Bold else FontWeight.Normal,
                                    color = if (config.scalerType == scaler) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(text = scaler.formula, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            FilterChip(
                                selected = config.scalerType == scaler,
                                onClick = { onConfigChange(config.copy(scalerType = scaler)) },
                                label = { Text(if (config.scalerType == scaler) "Active" else "Select") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = Color(0xFF030712)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Outlier Filtering Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Outlier Detection & Anomaly Trimming",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlierStrategy.values().forEach { strat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strat.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (config.outlierStrategy == strat) CyanPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            FilterChip(
                                selected = config.outlierStrategy == strat,
                                onClick = { onConfigChange(config.copy(outlierStrategy = strat)) },
                                label = { Text(if (config.outlierStrategy == strat) "Selected" else "Apply") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoSecondary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Synthetic Feature Expansion (Polynomials & Interactions)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Synthetic Feature Augmentation",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Polynomial Features (x², x³)", fontWeight = FontWeight.Medium)
                            Text(text = "Expands linear classifiers to non-linear spaces", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = config.addPolynomialFeatures,
                            onCheckedChange = { onConfigChange(config.copy(addPolynomialFeatures = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Feature Interaction Ratios (x₁ / x₂)", fontWeight = FontWeight.Medium)
                            Text(text = "Synthesizes cross-column correlation multipliers", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = config.addFeatureRatios,
                            onCheckedChange = { onConfigChange(config.copy(addFeatureRatios = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                        )
                    }
                }
            }
        }

        // Live Vector Transformation Preview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transformed Vector Sample (First Record)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val rawFirst = dataset.numericFeatures.firstOrNull() ?: emptyList()
                    val previewScaled = rawFirst.mapIndexed { idx, v ->
                        when (config.scalerType) {
                            ScalerType.STANDARD_Z_SCORE -> {
                                val mean = dataset.numericFeatures.map { it[idx] }.average()
                                val std = kotlin.math.sqrt(dataset.numericFeatures.map { kotlin.math.pow(it[idx] - mean, 2.0) }.average()).coerceAtLeast(0.01)
                                (v - mean) / std
                            }
                            ScalerType.MIN_MAX_SCALER -> {
                                val minV = dataset.numericFeatures.minOf { it[idx] }
                                val maxV = dataset.numericFeatures.maxOf { it[idx] }
                                val denom = (maxV - minV).coerceAtLeast(0.01)
                                (v - minV) / denom
                            }
                            else -> v
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090D16), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Raw Input: [${rawFirst.take(4).joinToString(", ") { String.format("%.2f", it) }}...]",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Scaled Output: [${previewScaled.take(4).joinToString(", ") { String.format("%.3f", it) }}...]",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }
                    }
                }
            }
        }

        // Proceed to Train Action Button
        item {
            Button(
                onClick = onProceedToTrain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF030712))
            ) {
                Icon(imageVector = Icons.Default.ModelTraining, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Proceed to Model Training Studio", fontWeight = FontWeight.Bold)
            }
        }
    }
}
