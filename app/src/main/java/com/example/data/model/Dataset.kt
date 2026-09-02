package com.example.data.model

enum class DatasetCategory(val displayName: String, val iconName: String) {
    TABULAR("Tabular", "table_chart"),
    VISION("Vision / Image", "image"),
    NLP("NLP / Text", "article"),
    SENSOR("Sensor & IoT", "sensors"),
    TIME_SERIES("Time Series", "timeline"),
    AUDIO("Audio & Speech", "graphic_eq")
}

enum class TaskType(val displayName: String) {
    CLASSIFICATION("Classification"),
    REGRESSION("Regression"),
    CLUSTERING("Clustering"),
    SENTIMENT("Sentiment Analysis"),
    FEATURE_DETECTION("Feature Detection")
}

enum class DataType {
    NUMERIC,
    CATEGORICAL,
    TEXT,
    BOOLEAN
}

enum class ScalerType(val displayName: String, val formula: String) {
    STANDARD_Z_SCORE("Standard Z-Score", "x' = (x - μ) / σ"),
    MIN_MAX_SCALER("Min-Max Scaler [0, 1]", "x' = (x - min) / (max - min)"),
    ROBUST_SCALER("Robust Scaler (IQR)", "x' = (x - median) / IQR"),
    L2_NORMALIZER("L2 Unit Normalizer", "x' = x / ||x||_2"),
    NONE("Raw Features (No Scaling)", "x' = x")
}

enum class OutlierStrategy(val displayName: String) {
    KEEP_ALL("Keep All Outliers"),
    IQR_TRIM_1_5("Trim 1.5x IQR Outliers"),
    Z_SCORE_3_SIGMA("Filter 3-Sigma Z-Scores (> 3.0)")
}

data class PreprocessingConfig(
    val scalerType: ScalerType = ScalerType.STANDARD_Z_SCORE,
    val outlierStrategy: OutlierStrategy = OutlierStrategy.KEEP_ALL,
    val addPolynomialFeatures: Boolean = false,
    val addFeatureRatios: Boolean = false,
    val imputeMissingMean: Boolean = true,
    val trainRatio: Float = 0.8f,
    val stratifySplit: Boolean = true
)

data class ColumnStat(
    val name: String,
    val dataType: DataType,
    val isTarget: Boolean = false,
    val min: Double = 0.0,
    val max: Double = 0.0,
    val mean: Double = 0.0,
    val median: Double = 0.0,
    val stdDev: Double = 0.0,
    val q1: Double = 0.0,
    val q3: Double = 0.0,
    val skewness: Double = 0.0,
    val uniqueValues: List<String> = emptyList(),
    val missingCount: Int = 0,
    val histogramBins: List<Pair<Double, Int>> = emptyList()
)

data class FeatureCorrelation(
    val featureA: String,
    val featureB: String,
    val pearsonR: Double
)

data class Dataset(
    val id: String,
    val title: String,
    val description: String,
    val category: DatasetCategory,
    val taskType: TaskType,
    val samplesCount: Int,
    val featuresCount: Int,
    val targetColumn: String,
    val license: String = "MIT / Open-Data",
    val difficulty: String = "Beginner",
    val tags: List<String> = emptyList(),
    val columnStats: List<ColumnStat> = emptyList(),
    val featureNames: List<String> = emptyList(),
    val classLabels: List<String> = emptyList(),
    val sampleRows: List<Map<String, String>> = emptyList(),
    // Raw numeric feature vectors for training in the engine (X) and target array (y)
    val numericFeatures: List<List<Double>> = emptyList(),
    val numericTargets: List<Double> = emptyList(), // For regression or integer class indices for classification
    val correlations: List<FeatureCorrelation> = emptyList(),
    val isCustom: Boolean = false,
    val rating: Double = 4.8,
    val downloadsCount: Int = 1240
)
