package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dataset
import com.example.data.model.DatasetCategory
import com.example.data.model.TaskType
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndigoSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DatasetHubScreen(
    datasets: List<Dataset>,
    searchQuery: String,
    selectedCategory: DatasetCategory?,
    selectedTaskType: TaskType?,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (DatasetCategory?) -> Unit,
    onTaskTypeSelect: (TaskType?) -> Unit,
    onDatasetClick: (Dataset) -> Unit,
    onTrainClick: (Dataset) -> Unit,
    onAiForgeClick: () -> Unit,
    onManualCreateClick: () -> Unit,
    onImportCsvClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner
            item {
                HubHeroCard(onAiForgeClick = onAiForgeClick)
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search datasets by name, domain, tags...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CyanPrimary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )
            }

            // Category Filter Chips
            item {
                Column {
                    Text(
                        text = "Data Modality",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelect(null) },
                            label = { Text("All Modalities") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Color(0xFF030712)
                            )
                        )
                        DatasetCategory.values().forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { onCategorySelect(if (selectedCategory == cat) null else cat) },
                                label = { Text(cat.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = Color(0xFF030712)
                                )
                            )
                        }
                    }
                }
            }

            // Task Type Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTaskType == null,
                        onClick = { onTaskTypeSelect(null) },
                        label = { Text("All Tasks") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoSecondary,
                            selectedLabelColor = Color.White
                        )
                    )
                    TaskType.values().forEach { task ->
                        FilterChip(
                            selected = selectedTaskType == task,
                            onClick = { onTaskTypeSelect(if (selectedTaskType == task) null else task) },
                            label = { Text(task.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoSecondary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Results count header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Datasets (${datasets.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dataset Cards
            if (datasets.isEmpty()) {
                item {
                    EmptyStateCard(onResetFilters = {
                        onSearchChange("")
                        onCategorySelect(null)
                        onTaskTypeSelect(null)
                    })
                }
            } else {
                items(datasets, key = { it.id }) { dataset ->
                    DatasetItemCard(
                        dataset = dataset,
                        onClick = { onDatasetClick(dataset) },
                        onTrainClick = { onTrainClick(dataset) }
                    )
                }
            }
        }

        // Floating Action Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = onImportCsvClick,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = CyanPrimary,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = "Import CSV / Data")
            }

            FloatingActionButton(
                onClick = onManualCreateClick,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = CyanPrimary,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Manual Dataset")
            }

            ExtendedFloatingActionButton(
                onClick = onAiForgeClick,
                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null) },
                text = { Text("AI Synthesizer", fontWeight = FontWeight.Bold) },
                containerColor = CyanPrimary,
                contentColor = Color(0xFF030712),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun HubHeroCard(onAiForgeClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0E1A34),
                            Color(0xFF1E293B)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyanPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ModelTraining, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DataForge ML",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Curated Training Data & On-Device ML",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Supply machine models with clean feature sets, train neural networks in real-time, and export production PyTorch & TensorFlow scripts.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DatasetItemCard(
    dataset: Dataset,
    onClick: () -> Unit,
    onTrainClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dataset.isCustom) {
                            Surface(
                                color = IndigoSecondary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = "CUSTOM",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = dataset.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dataset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges & Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricChip(label = "${dataset.samplesCount} Samples", color = CyanPrimary)
                MetricChip(label = "${dataset.featuresCount} Features", color = IndigoSecondary)
                MetricChip(label = dataset.taskType.displayName, color = Color(0xFF10B981))
                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    onClick = onTrainClick,
                    shape = RoundedCornerShape(10.dp),
                    color = CyanPrimary,
                    contentColor = Color(0xFF030712)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ModelTraining, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Train", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyStateCard(onResetFilters: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "No matching datasets found", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Try adjusting your search terms or filter selections.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                onClick = onResetFilters,
                shape = RoundedCornerShape(8.dp),
                color = CyanPrimary,
                contentColor = Color(0xFF030712)
            ) {
                Text(text = "Reset Filters", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}
