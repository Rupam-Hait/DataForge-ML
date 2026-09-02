package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TrainingRunEntity
import com.example.data.model.Dataset
import com.example.data.model.DatasetCategory
import com.example.data.model.Hyperparameters
import com.example.data.model.MLModelType
import com.example.data.model.PredictionResult
import com.example.data.model.TaskType
import com.example.data.model.TrainingProgressState
import com.example.data.repository.DatasetRepository
import com.example.data.repository.PreloadedDatasets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    HUB("Dataset Hub"),
    DETAIL("Inspect & EDA"),
    PREPROCESS("Preprocessing"),
    TRAINER("Model Trainer"),
    EXPORT("Code & API"),
    HISTORY("Runs History")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DatasetRepository = DatasetRepository(
        AppDatabase.getDatabase(application).datasetDao()
    )

    private val _currentTab = MutableStateFlow(AppNavTab.HUB)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<DatasetCategory?>(null)
    val selectedCategory: StateFlow<DatasetCategory?> = _selectedCategory.asStateFlow()

    private val _selectedTaskType = MutableStateFlow<TaskType?>(null)
    val selectedTaskType: StateFlow<TaskType?> = _selectedTaskType.asStateFlow()

    private val _selectedDataset = MutableStateFlow<Dataset>(PreloadedDatasets.irisDataset)
    val selectedDataset: StateFlow<Dataset> = _selectedDataset.asStateFlow()

    private val _preprocessingConfig = MutableStateFlow(com.example.data.model.PreprocessingConfig())
    val preprocessingConfig: StateFlow<com.example.data.model.PreprocessingConfig> = _preprocessingConfig.asStateFlow()

    private val _hyperparameters = MutableStateFlow(Hyperparameters())
    val hyperparameters: StateFlow<Hyperparameters> = _hyperparameters.asStateFlow()

    private val _trainingProgress = MutableStateFlow(TrainingProgressState())
    val trainingProgress: StateFlow<TrainingProgressState> = _trainingProgress.asStateFlow()

    private val _predictionInputs = MutableStateFlow<Map<String, Double>>(emptyMap())
    val predictionInputs: StateFlow<Map<String, Double>> = _predictionInputs.asStateFlow()

    private val _predictionResult = MutableStateFlow<PredictionResult?>(null)
    val predictionResult: StateFlow<PredictionResult?> = _predictionResult.asStateFlow()

    private val _modelAdvisorTip = MutableStateFlow("")
    val modelAdvisorTip: StateFlow<String> = _modelAdvisorTip.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val filteredDatasets: StateFlow<List<Dataset>> = combine(
        repository.allDatasetsFlow,
        _searchQuery,
        _selectedCategory,
        _selectedTaskType
    ) { all, query, cat, task ->
        all.filter { ds ->
            val matchesQuery = query.isBlank() ||
                    ds.title.contains(query, ignoreCase = true) ||
                    ds.description.contains(query, ignoreCase = true) ||
                    ds.tags.any { it.contains(query, ignoreCase = true) }

            val matchesCat = cat == null || ds.category == cat
            val matchesTask = task == null || ds.taskType == task
            matchesQuery && matchesCat && matchesTask
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PreloadedDatasets.getAll()
    )

    val trainingHistory: StateFlow<List<TrainingRunEntity>> = repository.trainingHistoryFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        selectDataset(PreloadedDatasets.irisDataset)
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: DatasetCategory?) {
        _selectedCategory.value = category
    }

    fun setTaskTypeFilter(taskType: TaskType?) {
        _selectedTaskType.value = taskType
    }

    fun selectDataset(dataset: Dataset) {
        _selectedDataset.value = dataset
        // Auto initialize prediction inputs with sample mean values
        val initialInputs = mutableMapOf<String, Double>()
        dataset.featureNames.forEachIndexed { idx, name ->
            val defaultVal = if (dataset.numericFeatures.isNotEmpty()) {
                dataset.numericFeatures.map { it.getOrElse(idx) { 0.0 } }.average()
            } else 1.0
            initialInputs[name] = defaultVal
        }
        _predictionInputs.value = initialInputs
        _predictionResult.value = null
        _trainingProgress.value = TrainingProgressState()

        // Match optimal default model type for task
        val defaultModel = when (dataset.taskType) {
            TaskType.REGRESSION -> MLModelType.LINEAR_REGRESSION
            TaskType.CLUSTERING -> MLModelType.K_MEANS_CLUSTERING
            else -> MLModelType.NEURAL_NETWORK_MLP
        }
        _hyperparameters.value = _hyperparameters.value.copy(modelType = defaultModel)

        // Load advisor advice in background
        viewModelScope.launch {
            _modelAdvisorTip.value = repository.getModelAdvisorTip(dataset)
        }
    }

    fun updateHyperparameters(update: (Hyperparameters) -> Hyperparameters) {
        _hyperparameters.value = update(_hyperparameters.value)
    }

    fun startTraining() {
        val dataset = _selectedDataset.value
        val params = _hyperparameters.value

        viewModelScope.launch {
            _trainingProgress.value = TrainingProgressState(isTraining = true, totalEpochs = params.epochs)
            val finalState = repository.trainModel(dataset, params) { progress ->
                _trainingProgress.value = progress
            }
            _trainingProgress.value = finalState

            // Run initial prediction after training completes
            runPrediction()
        }
    }

    fun updatePredictionInput(featureName: String, value: Double) {
        val current = _predictionInputs.value.toMutableMap()
        current[featureName] = value
        _predictionInputs.value = current
        runPrediction()
    }

    fun runPrediction() {
        val dataset = _selectedDataset.value
        val inputsList = dataset.featureNames.map { name ->
            _predictionInputs.value[name] ?: 0.0
        }
        val result = repository.predict(inputsList, _hyperparameters.value)
        _predictionResult.value = result
    }

    fun generateAiDataset(prompt: String, category: DatasetCategory, taskType: TaskType) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            try {
                val newDataset = repository.generateAiDataset(prompt, category, taskType)
                selectDataset(newDataset)
                _toastMessage.value = "Dataset '${newDataset.title}' synthesized & added to library!"
                _currentTab.value = AppNavTab.DETAIL
            } catch (e: Exception) {
                _toastMessage.value = "Dataset creation notice: ${e.message}"
            } finally {
                _isGeneratingAi.value = false
            }
        }
    }

    fun updatePreprocessingConfig(config: com.example.data.model.PreprocessingConfig) {
        _preprocessingConfig.value = config
        _hyperparameters.value = _hyperparameters.value.copy(
            normalizeFeatures = config.scalerType != com.example.data.model.ScalerType.NONE
        )
    }

    fun importCustomDataset(dataset: Dataset) {
        viewModelScope.launch {
            repository.saveCustomDataset(dataset)
            selectDataset(dataset)
            _toastMessage.value = "Dataset '${dataset.title}' successfully ingested and parsed!"
            _currentTab.value = AppNavTab.DETAIL
        }
    }

    fun saveCustomDataset(dataset: Dataset) {
        viewModelScope.launch {
            repository.saveCustomDataset(dataset)
            selectDataset(dataset)
            _toastMessage.value = "Custom dataset '${dataset.title}' saved!"
            _currentTab.value = AppNavTab.DETAIL
        }
    }

    fun deleteCustomDataset(datasetId: String) {
        viewModelScope.launch {
            repository.deleteCustomDataset(datasetId)
            selectDataset(PreloadedDatasets.irisDataset)
            _toastMessage.value = "Dataset removed from local storage."
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
