package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_datasets")
data class DatasetEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val categoryName: String,
    val taskTypeName: String,
    val samplesCount: Int,
    val featuresCount: Int,
    val targetColumn: String,
    val license: String,
    val difficulty: String,
    val tagsJson: String,
    val featureNamesJson: String,
    val classLabelsJson: String,
    val sampleRowsJson: String,
    val numericFeaturesJson: String,
    val numericTargetsJson: String,
    val isCustom: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "training_runs")
data class TrainingRunEntity(
    @PrimaryKey(autoGenerate = true)
    val runId: Long = 0,
    val datasetId: String,
    val datasetTitle: String,
    val modelType: String,
    val finalTrainLoss: Double,
    val finalValLoss: Double,
    val finalTrainAcc: Double,
    val finalValAcc: Double,
    val r2Score: Double,
    val epochs: Int,
    val learningRate: Double,
    val timestamp: Long = System.currentTimeMillis()
)
