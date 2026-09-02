package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ModelTraining
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.dialogs.AiDatasetGeneratorDialog
import com.example.ui.dialogs.DatasetImportDialog
import com.example.ui.dialogs.ManualDatasetCreateDialog
import com.example.ui.screens.CodeExportScreen
import com.example.ui.screens.DatasetDetailScreen
import com.example.ui.screens.DatasetHubScreen
import com.example.ui.screens.ModelTrainerScreen
import com.example.ui.screens.PreprocessingStudioScreen
import com.example.ui.screens.TrainingHistoryScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DataForgeApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataForgeApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val filteredDatasets by viewModel.filteredDatasets.collectAsState()
    val selectedDataset by viewModel.selectedDataset.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTaskType by viewModel.selectedTaskType.collectAsState()
    val preprocessingConfig by viewModel.preprocessingConfig.collectAsState()
    val hyperparameters by viewModel.hyperparameters.collectAsState()
    val trainingProgress by viewModel.trainingProgress.collectAsState()
    val predictionInputs by viewModel.predictionInputs.collectAsState()
    val predictionResult by viewModel.predictionResult.collectAsState()
    val modelAdvisorTip by viewModel.modelAdvisorTip.collectAsState()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsState()
    val trainingHistory by viewModel.trainingHistory.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var showAiForgeDialog by remember { mutableStateOf(false) }
    var showManualCreateDialog by remember { mutableStateOf(false) }
    var showImportCsvDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DataForge ML",
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppNavTab.HUB,
                    onClick = { viewModel.selectTab(AppNavTab.HUB) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.HUB) Icons.Filled.Storage else Icons.Outlined.Storage,
                            contentDescription = "Hub",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Hub") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF030712),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_hub")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.DETAIL,
                    onClick = { viewModel.selectTab(AppNavTab.DETAIL) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.DETAIL) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = "Inspect",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("EDA") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF030712),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_detail")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.PREPROCESS,
                    onClick = { viewModel.selectTab(AppNavTab.PREPROCESS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.PREPROCESS) Icons.Filled.Transform else Icons.Outlined.Transform,
                            contentDescription = "Preprocess",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Pipeline") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF030712),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_preprocess")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.TRAINER,
                    onClick = { viewModel.selectTab(AppNavTab.TRAINER) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.TRAINER) Icons.Filled.ModelTraining else Icons.Outlined.ModelTraining,
                            contentDescription = "Train",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Train") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF030712),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_trainer")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.EXPORT,
                    onClick = { viewModel.selectTab(AppNavTab.EXPORT) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.EXPORT) Icons.Filled.Code else Icons.Outlined.Code,
                            contentDescription = "Code",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Export") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF030712),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_export")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.HISTORY,
                    onClick = { viewModel.selectTab(AppNavTab.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Runs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF030712),
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    AppNavTab.HUB -> DatasetHubScreen(
                        datasets = filteredDatasets,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        selectedTaskType = selectedTaskType,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategorySelect = { viewModel.setCategoryFilter(it) },
                        onTaskTypeSelect = { viewModel.setTaskTypeFilter(it) },
                        onDatasetClick = {
                            viewModel.selectDataset(it)
                            viewModel.selectTab(AppNavTab.DETAIL)
                        },
                        onTrainClick = {
                            viewModel.selectDataset(it)
                            viewModel.selectTab(AppNavTab.TRAINER)
                        },
                        onAiForgeClick = { showAiForgeDialog = true },
                        onManualCreateClick = { showManualCreateDialog = true },
                        onImportCsvClick = { showImportCsvDialog = true }
                    )

                    AppNavTab.DETAIL -> DatasetDetailScreen(
                        dataset = selectedDataset,
                        advisorTip = modelAdvisorTip,
                        onBackClick = { viewModel.selectTab(AppNavTab.HUB) },
                        onPreprocessClick = { viewModel.selectTab(AppNavTab.PREPROCESS) },
                        onTrainClick = { viewModel.selectTab(AppNavTab.TRAINER) },
                        onExportClick = { viewModel.selectTab(AppNavTab.EXPORT) },
                        onDeleteClick = {
                            viewModel.deleteCustomDataset(it)
                            viewModel.selectTab(AppNavTab.HUB)
                        }
                    )

                    AppNavTab.PREPROCESS -> PreprocessingStudioScreen(
                        dataset = selectedDataset,
                        config = preprocessingConfig,
                        onConfigChange = { viewModel.updatePreprocessingConfig(it) },
                        onProceedToTrain = { viewModel.selectTab(AppNavTab.TRAINER) },
                        onBackClick = { viewModel.selectTab(AppNavTab.DETAIL) }
                    )

                    AppNavTab.TRAINER -> ModelTrainerScreen(
                        dataset = selectedDataset,
                        hyperparams = hyperparameters,
                        trainingProgress = trainingProgress,
                        predictionInputs = predictionInputs,
                        predictionResult = predictionResult,
                        onHyperparamsChange = { viewModel.updateHyperparameters(it) },
                        onStartTraining = { viewModel.startTraining() },
                        onPredictionInputChange = { name, value -> viewModel.updatePredictionInput(name, value) },
                        onExportCodeClick = { viewModel.selectTab(AppNavTab.EXPORT) },
                        onBackClick = { viewModel.selectTab(AppNavTab.DETAIL) }
                    )

                    AppNavTab.EXPORT -> CodeExportScreen(
                        dataset = selectedDataset,
                        hyperparams = hyperparameters,
                        onBackClick = { viewModel.selectTab(AppNavTab.TRAINER) }
                    )

                    AppNavTab.HISTORY -> TrainingHistoryScreen(
                        runs = trainingHistory
                    )
                }
            }
        }
    }

    if (showAiForgeDialog) {
        AiDatasetGeneratorDialog(
            isGenerating = isGeneratingAi,
            onDismiss = { showAiForgeDialog = false },
            onGenerate = { prompt, category, taskType ->
                viewModel.generateAiDataset(prompt, category, taskType)
                showAiForgeDialog = false
            }
        )
    }

    if (showManualCreateDialog) {
        ManualDatasetCreateDialog(
            onDismiss = { showManualCreateDialog = false },
            onSave = { dataset ->
                viewModel.saveCustomDataset(dataset)
                showManualCreateDialog = false
            }
        )
    }

    if (showImportCsvDialog) {
        DatasetImportDialog(
            onDismiss = { showImportCsvDialog = false },
            onDatasetImported = { dataset ->
                viewModel.importCustomDataset(dataset)
                showImportCsvDialog = false
            }
        )
    }
}


