package com.example.data.repository

import com.example.ai.GeminiDatasetService
import com.example.data.local.DatasetDao
import com.example.data.local.DatasetEntity
import com.example.data.local.TrainingRunEntity
import com.example.data.model.Dataset
import com.example.data.model.DatasetCategory
import com.example.data.model.Hyperparameters
import com.example.data.model.PredictionResult
import com.example.data.model.TaskType
import com.example.data.model.TrainingProgressState
import com.example.ml.engine.MLEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class DatasetRepository(
    private val dao: DatasetDao,
    private val mlEngine: MLEngine = MLEngine()
) {

    // Combines preloaded curated datasets with user-created / AI-generated datasets in Room
    val allDatasetsFlow: Flow<List<Dataset>> = dao.getAllCustomDatasets().map { entities ->
        val customDatasets = entities.map { entityToDataset(it) }
        PreloadedDatasets.getAll() + customDatasets
    }

    val trainingHistoryFlow: Flow<List<TrainingRunEntity>> = dao.getAllTrainingRuns()

    suspend fun saveCustomDataset(dataset: Dataset) {
        val entity = datasetToEntity(dataset)
        dao.insertDataset(entity)
    }

    suspend fun deleteCustomDataset(datasetId: String) {
        dao.deleteDatasetById(datasetId)
    }

    suspend fun trainModel(
        dataset: Dataset,
        hyperparams: Hyperparameters,
        onProgress: (TrainingProgressState) -> Unit
    ): TrainingProgressState {
        val finalState = mlEngine.train(dataset, hyperparams, onProgress)

        // Save experiment run to history
        if (finalState.isCompleted) {
            val run = TrainingRunEntity(
                datasetId = dataset.id,
                datasetTitle = dataset.title,
                modelType = hyperparams.modelType.title,
                finalTrainLoss = finalState.currentTrainLoss,
                finalValLoss = finalState.currentValLoss,
                finalTrainAcc = finalState.currentTrainAcc,
                finalValAcc = finalState.currentValAcc,
                r2Score = finalState.r2Score,
                epochs = hyperparams.epochs,
                learningRate = hyperparams.learningRate
            )
            dao.insertTrainingRun(run)
        }

        return finalState
    }

    fun predict(inputFeatures: List<Double>, hyperparams: Hyperparameters): PredictionResult {
        return mlEngine.predict(inputFeatures, hyperparams.activation)
    }

    suspend fun generateAiDataset(prompt: String, category: DatasetCategory, taskType: TaskType): Dataset {
        val dataset = GeminiDatasetService.generateSyntheticDataset(prompt, category, taskType)
        saveCustomDataset(dataset)
        return dataset
    }

    suspend fun getModelAdvisorTip(dataset: Dataset): String {
        return GeminiDatasetService.getModelAdvisorRecommendation(dataset)
    }

    private fun datasetToEntity(dataset: Dataset): DatasetEntity {
        val sampleRowsJson = JSONArray().apply {
            dataset.sampleRows.forEach { map ->
                val obj = JSONObject()
                map.forEach { (k, v) -> obj.put(k, v) }
                put(obj)
            }
        }.toString()

        val numericFeaturesJson = JSONArray().apply {
            dataset.numericFeatures.forEach { row ->
                val rowArr = JSONArray()
                row.forEach { rowArr.put(it) }
                put(rowArr)
            }
        }.toString()

        val numericTargetsJson = JSONArray().apply {
            dataset.numericTargets.forEach { put(it) }
        }.toString()

        val tagsJson = JSONArray(dataset.tags).toString()
        val featureNamesJson = JSONArray(dataset.featureNames).toString()
        val classLabelsJson = JSONArray(dataset.classLabels).toString()

        return DatasetEntity(
            id = dataset.id,
            title = dataset.title,
            description = dataset.description,
            categoryName = dataset.category.name,
            taskTypeName = dataset.taskType.name,
            samplesCount = dataset.samplesCount,
            featuresCount = dataset.featuresCount,
            targetColumn = dataset.targetColumn,
            license = dataset.license,
            difficulty = dataset.difficulty,
            tagsJson = tagsJson,
            featureNamesJson = featureNamesJson,
            classLabelsJson = classLabelsJson,
            sampleRowsJson = sampleRowsJson,
            numericFeaturesJson = numericFeaturesJson,
            numericTargetsJson = numericTargetsJson,
            isCustom = true
        )
    }

    private fun entityToDataset(entity: DatasetEntity): Dataset {
        val category = try {
            DatasetCategory.valueOf(entity.categoryName)
        } catch (e: Exception) {
            DatasetCategory.TABULAR
        }
        val taskType = try {
            TaskType.valueOf(entity.taskTypeName)
        } catch (e: Exception) {
            TaskType.CLASSIFICATION
        }

        val tags = mutableListOf<String>()
        val tagsArr = JSONArray(entity.tagsJson)
        for (i in 0 until tagsArr.length()) tags.add(tagsArr.getString(i))

        val featureNames = mutableListOf<String>()
        val featArr = JSONArray(entity.featureNamesJson)
        for (i in 0 until featArr.length()) featureNames.add(featArr.getString(i))

        val classLabels = mutableListOf<String>()
        val classArr = JSONArray(entity.classLabelsJson)
        for (i in 0 until classArr.length()) classLabels.add(classArr.getString(i))

        val sampleRows = mutableListOf<Map<String, String>>()
        val rowsArr = JSONArray(entity.sampleRowsJson)
        for (i in 0 until rowsArr.length()) {
            val obj = rowsArr.getJSONObject(i)
            val map = mutableMapOf<String, String>()
            obj.keys().forEach { k -> map[k] = obj.getString(k) }
            sampleRows.add(map)
        }

        val numericFeatures = mutableListOf<List<Double>>()
        val numFeatArr = JSONArray(entity.numericFeaturesJson)
        for (i in 0 until numFeatArr.length()) {
            val rowArr = numFeatArr.getJSONArray(i)
            val rowList = mutableListOf<Double>()
            for (j in 0 until rowArr.length()) rowList.add(rowArr.getDouble(j))
            numericFeatures.add(rowList)
        }

        val numericTargets = mutableListOf<Double>()
        val numTargArr = JSONArray(entity.numericTargetsJson)
        for (i in 0 until numTargArr.length()) numericTargets.add(numTargArr.getDouble(i))

        return Dataset(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = category,
            taskType = taskType,
            samplesCount = entity.samplesCount,
            featuresCount = entity.featuresCount,
            targetColumn = entity.targetColumn,
            license = entity.license,
            difficulty = entity.difficulty,
            tags = tags,
            classLabels = classLabels,
            featureNames = featureNames,
            sampleRows = sampleRows,
            numericFeatures = numericFeatures,
            numericTargets = numericTargets,
            isCustom = true
        )
    }
}
