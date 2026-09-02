package com.example.data.repository

import com.example.data.model.ColumnStat
import com.example.data.model.DataType
import com.example.data.model.Dataset
import com.example.data.model.DatasetCategory
import com.example.data.model.TaskType

object PreloadedDatasets {

    fun getAll(): List<Dataset> = listOf(
        irisDataset,
        heartDiseaseDataset,
        californiaHousingDataset,
        customerChurnDataset,
        wineQualityDataset,
        cyberSecurityDataset,
        solarPowerDataset,
        defectInspectionDataset,
        audioKeywordDataset,
        sentimentNlpDataset
    ).map { ds ->
        val corr = com.example.ml.engine.MLEngine.computePearsonCorrelations(ds)
        val stats = com.example.ml.engine.MLEngine.computeColumnStats(ds)
        ds.copy(correlations = corr, columnStats = if (stats.isNotEmpty()) stats else ds.columnStats)
    }

    // 1. Iris Flower Classification (Classic Benchmark)
    val irisDataset: Dataset by lazy {
        val features = listOf(
            listOf(5.1, 3.5, 1.4, 0.2), listOf(4.9, 3.0, 1.4, 0.2), listOf(4.7, 3.2, 1.3, 0.2),
            listOf(4.6, 3.1, 1.5, 0.2), listOf(5.0, 3.6, 1.4, 0.2), listOf(5.4, 3.9, 1.7, 0.4),
            listOf(4.6, 3.4, 1.4, 0.3), listOf(5.0, 3.4, 1.5, 0.2), listOf(4.4, 2.9, 1.4, 0.2),
            listOf(4.9, 3.1, 1.5, 0.1), listOf(5.4, 3.7, 1.5, 0.2), listOf(4.8, 3.4, 1.6, 0.2),
            listOf(4.8, 3.0, 1.4, 0.1), listOf(4.3, 3.0, 1.1, 0.1), listOf(5.8, 4.0, 1.2, 0.2),
            listOf(5.7, 4.4, 1.5, 0.4), listOf(5.4, 3.9, 1.3, 0.4), listOf(5.1, 3.5, 1.4, 0.3),
            listOf(5.7, 3.8, 1.7, 0.3), listOf(5.1, 3.8, 1.5, 0.3), listOf(5.4, 3.4, 1.7, 0.2),
            listOf(5.1, 3.7, 1.5, 0.4), listOf(4.6, 3.6, 1.0, 0.2), listOf(5.1, 3.3, 1.7, 0.5),
            // Versicolor (Class 1)
            listOf(7.0, 3.2, 4.7, 1.4), listOf(6.4, 3.2, 4.5, 1.5), listOf(6.9, 3.1, 4.9, 1.5),
            listOf(5.5, 2.3, 4.0, 1.3), listOf(6.5, 2.8, 4.6, 1.5), listOf(5.7, 2.8, 4.5, 1.3),
            listOf(6.3, 3.3, 4.7, 1.6), listOf(4.9, 2.4, 3.3, 1.0), listOf(6.6, 2.9, 4.6, 1.3),
            listOf(5.2, 2.7, 3.9, 1.4), listOf(5.0, 2.0, 3.5, 1.0), listOf(5.9, 3.0, 4.2, 1.5),
            listOf(6.0, 2.2, 4.0, 1.0), listOf(6.1, 2.9, 4.7, 1.4), listOf(5.6, 2.9, 3.6, 1.3),
            listOf(6.7, 3.1, 4.4, 1.4), listOf(5.6, 3.0, 4.5, 1.5), listOf(5.8, 2.7, 4.1, 1.0),
            listOf(6.2, 2.2, 4.5, 1.5), listOf(5.6, 2.5, 3.9, 1.1), listOf(5.9, 3.2, 4.8, 1.8),
            listOf(6.1, 2.8, 4.0, 1.3), listOf(6.3, 2.5, 4.9, 1.5), listOf(6.1, 2.8, 4.7, 1.2),
            // Virginica (Class 2)
            listOf(6.3, 3.3, 6.0, 2.5), listOf(5.8, 2.7, 5.1, 1.9), listOf(7.1, 3.0, 5.9, 2.1),
            listOf(6.3, 2.9, 5.6, 1.8), listOf(6.5, 3.0, 5.8, 2.2), listOf(7.6, 3.0, 6.6, 2.1),
            listOf(4.9, 2.5, 4.5, 1.7), listOf(7.3, 2.9, 6.3, 1.8), listOf(6.7, 2.5, 5.8, 1.8),
            listOf(7.2, 3.6, 6.1, 2.5), listOf(6.5, 3.2, 5.1, 2.0), listOf(6.4, 2.7, 5.3, 1.9),
            listOf(6.8, 3.0, 5.5, 2.1), listOf(5.7, 2.5, 5.0, 2.0), listOf(5.8, 2.8, 5.1, 2.4),
            listOf(6.4, 3.2, 5.3, 2.3), listOf(6.5, 3.0, 5.5, 1.8), listOf(7.7, 3.8, 6.7, 2.2),
            listOf(7.7, 2.6, 6.9, 2.3), listOf(6.0, 2.2, 5.0, 1.5), listOf(6.9, 3.2, 5.7, 2.3),
            listOf(5.6, 2.8, 4.9, 2.0), listOf(7.7, 2.8, 6.7, 2.0), listOf(6.3, 2.7, 4.9, 1.8)
        )
        val targets = MutableList(24) { 0.0 } + MutableList(24) { 1.0 } + MutableList(24) { 2.0 }
        val classLabels = listOf("Setosa", "Versicolor", "Virginica")
        val featureNames = listOf("Sepal Length (cm)", "Sepal Width (cm)", "Petal Length (cm)", "Petal Width (cm)")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "sepal_length" to row[0].toString(),
                "sepal_width" to row[1].toString(),
                "petal_length" to row[2].toString(),
                "petal_width" to row[3].toString(),
                "species" to classLabels[targets[idx].toInt()]
            )
        }

        Dataset(
            id = "ds_iris_flower",
            title = "Iris Flower Botanical Dataset",
            description = "Standard multi-class benchmark for botanical pattern classification based on sepal and petal morphological measurements.",
            category = DatasetCategory.TABULAR,
            taskType = TaskType.CLASSIFICATION,
            samplesCount = features.size,
            featuresCount = 4,
            targetColumn = "species",
            difficulty = "Beginner",
            tags = listOf("Morphology", "Multi-Class", "Benchmark", "Botany"),
            classLabels = classLabels,
            featureNames = featureNames,
            columnStats = listOf(
                ColumnStat("sepal_length", DataType.NUMERIC, false, 4.3, 7.9, 5.84, 0.83),
                ColumnStat("sepal_width", DataType.NUMERIC, false, 2.0, 4.4, 3.05, 0.43),
                ColumnStat("petal_length", DataType.NUMERIC, false, 1.0, 6.9, 3.76, 1.76),
                ColumnStat("petal_width", DataType.NUMERIC, false, 0.1, 2.5, 1.20, 0.76),
                ColumnStat("species", DataType.CATEGORICAL, true, uniqueValues = classLabels)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.9,
            downloadsCount = 8920
        )
    }

    // 2. Heart Disease Risk Classifier (Medical ML)
    val heartDiseaseDataset: Dataset by lazy {
        val features = listOf(
            listOf(63.0, 145.0, 233.0, 150.0, 2.3, 1.0),
            listOf(37.0, 130.0, 250.0, 187.0, 3.5, 0.0),
            listOf(41.0, 130.0, 204.0, 172.0, 1.4, 0.0),
            listOf(56.0, 120.0, 236.0, 178.0, 0.8, 0.0),
            listOf(57.0, 120.0, 354.0, 163.0, 0.6, 1.0),
            listOf(57.0, 140.0, 192.0, 148.0, 0.4, 0.0),
            listOf(56.0, 140.0, 294.0, 153.0, 1.3, 0.0),
            listOf(44.0, 120.0, 263.0, 173.0, 0.0, 0.0),
            listOf(52.0, 172.0, 199.0, 162.0, 0.5, 0.0),
            listOf(57.0, 150.0, 168.0, 174.0, 1.6, 0.0),
            listOf(54.0, 140.0, 239.0, 160.0, 1.2, 0.0),
            listOf(48.0, 130.0, 275.0, 139.0, 0.2, 0.0),
            listOf(49.0, 130.0, 266.0, 171.0, 0.6, 0.0),
            listOf(64.0, 110.0, 211.0, 144.0, 1.8, 1.0),
            listOf(58.0, 150.0, 283.0, 162.0, 1.0, 0.0),
            listOf(50.0, 120.0, 219.0, 158.0, 1.6, 0.0),
            listOf(58.0, 120.0, 340.0, 172.0, 0.0, 0.0),
            listOf(66.0, 150.0, 226.0, 114.0, 2.6, 0.0),
            listOf(43.0, 150.0, 247.0, 171.0, 1.5, 0.0),
            listOf(69.0, 140.0, 239.0, 151.0, 1.8, 0.0),
            listOf(59.0, 135.0, 234.0, 161.0, 0.5, 0.0),
            listOf(44.0, 130.0, 233.0, 179.0, 0.4, 0.0),
            listOf(42.0, 148.0, 244.0, 178.0, 0.8, 0.0),
            listOf(52.0, 128.0, 205.0, 184.0, 0.0, 0.0),
            // High Risk cases (Class 1)
            listOf(67.0, 160.0, 286.0, 108.0, 1.5, 1.0),
            listOf(67.0, 120.0, 229.0, 129.0, 2.6, 1.0),
            listOf(62.0, 130.0, 231.0, 103.0, 1.4, 1.0),
            listOf(53.0, 140.0, 203.0, 155.0, 3.1, 1.0),
            listOf(58.0, 114.0, 318.0, 140.0, 4.4, 0.0),
            listOf(58.0, 170.0, 225.0, 146.0, 2.8, 1.0),
            listOf(46.0, 140.0, 311.0, 120.0, 1.8, 1.0),
            listOf(53.0, 142.0, 226.0, 111.0, 0.0, 1.0),
            listOf(65.0, 135.0, 254.0, 127.0, 2.8, 1.0),
            listOf(48.0, 130.0, 256.0, 150.0, 0.0, 1.0),
            listOf(63.0, 130.0, 330.0, 132.0, 1.8, 1.0),
            listOf(65.0, 110.0, 248.0, 158.0, 0.6, 0.0),
            listOf(60.0, 140.0, 293.0, 170.0, 1.2, 1.0),
            listOf(59.0, 140.0, 177.0, 162.0, 0.0, 1.0),
            listOf(57.0, 140.0, 241.0, 123.0, 0.2, 1.0),
            listOf(61.0, 140.0, 207.0, 138.0, 1.9, 1.0),
            listOf(56.0, 130.0, 283.0, 103.0, 1.6, 1.0),
            listOf(64.0, 120.0, 246.0, 96.0, 2.2, 1.0),
            listOf(58.0, 100.0, 234.0, 156.0, 0.1, 0.0),
            listOf(47.0, 110.0, 275.0, 118.0, 1.0, 1.0),
            listOf(52.0, 125.0, 212.0, 168.0, 1.0, 0.0),
            listOf(58.0, 146.0, 218.0, 105.0, 2.0, 1.0),
            listOf(45.0, 115.0, 260.0, 185.0, 0.0, 0.0),
            listOf(53.0, 130.0, 264.0, 143.0, 0.4, 0.0)
        )
        val targets = MutableList(24) { 0.0 } + MutableList(24) { 1.0 }
        val classLabels = listOf("Low Risk (Healthy)", "High Risk (Cardiac)")
        val featureNames = listOf("Age (years)", "Resting Blood Pressure", "Serum Cholesterol", "Max Heart Rate", "ST Depression", "Exercise Angina")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "age" to row[0].toInt().toString(),
                "resting_bp" to row[1].toInt().toString(),
                "cholesterol" to row[2].toInt().toString(),
                "max_hr" to row[3].toInt().toString(),
                "st_depression" to row[4].toString(),
                "exercise_angina" to if (row[5] > 0.5) "Yes" else "No",
                "diagnosis" to classLabels[targets[idx].toInt()]
            )
        }

        Dataset(
            id = "ds_heart_disease",
            title = "Cardiovascular Clinical Diagnostic Dataset",
            description = "Biomedical patient parameters for training binary medical screening classifiers and diagnostic decision trees.",
            category = DatasetCategory.TABULAR,
            taskType = TaskType.CLASSIFICATION,
            samplesCount = features.size,
            featuresCount = 6,
            targetColumn = "diagnosis",
            difficulty = "Intermediate",
            tags = listOf("Healthcare", "Cardiology", "Diagnostics", "Binary Classification"),
            classLabels = classLabels,
            featureNames = featureNames,
            columnStats = listOf(
                ColumnStat("age", DataType.NUMERIC, false, 37.0, 69.0, 54.8, 7.8),
                ColumnStat("resting_bp", DataType.NUMERIC, false, 100.0, 172.0, 133.2, 16.4),
                ColumnStat("cholesterol", DataType.NUMERIC, false, 168.0, 354.0, 246.5, 38.2),
                ColumnStat("max_hr", DataType.NUMERIC, false, 96.0, 187.0, 149.3, 24.5),
                ColumnStat("st_depression", DataType.NUMERIC, false, 0.0, 4.4, 1.25, 1.02),
                ColumnStat("exercise_angina", DataType.NUMERIC, false, 0.0, 1.0, 0.38, 0.49),
                ColumnStat("diagnosis", DataType.CATEGORICAL, true, uniqueValues = classLabels)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.95,
            downloadsCount = 6430
        )
    }

    // 3. California Real Estate Housing Price (Continuous Regression)
    val californiaHousingDataset: Dataset by lazy {
        val features = listOf(
            listOf(8.32, 41.0, 6.98, 322.0, 2.55, 37.88),
            listOf(8.30, 21.0, 6.23, 2401.0, 2.10, 37.86),
            listOf(7.25, 52.0, 8.28, 496.0, 2.80, 37.85),
            listOf(5.64, 52.0, 5.81, 558.0, 2.54, 37.85),
            listOf(3.84, 52.0, 6.28, 565.0, 2.18, 37.85),
            listOf(4.03, 52.0, 4.76, 413.0, 2.13, 37.85),
            listOf(3.65, 52.0, 4.93, 1094.0, 2.12, 37.84),
            listOf(3.12, 52.0, 4.79, 1157.0, 1.78, 37.84),
            listOf(2.08, 42.0, 4.29, 1206.0, 2.02, 37.84),
            listOf(3.69, 52.0, 4.97, 1551.0, 2.17, 37.84),
            listOf(3.20, 52.0, 5.47, 910.0, 2.29, 37.85),
            listOf(3.27, 52.0, 4.77, 1504.0, 2.13, 37.85),
            listOf(3.07, 52.0, 5.32, 1098.0, 2.30, 37.85),
            listOf(2.67, 52.0, 4.04, 345.0, 1.98, 37.84),
            listOf(1.91, 52.0, 4.26, 1212.0, 1.94, 37.85),
            listOf(2.12, 50.0, 4.24, 697.0, 2.64, 37.85),
            listOf(2.77, 52.0, 5.44, 990.0, 2.92, 37.85),
            listOf(2.12, 52.0, 4.05, 648.0, 1.95, 37.85),
            listOf(1.99, 50.0, 5.34, 990.0, 2.36, 37.84),
            listOf(2.60, 52.0, 5.46, 690.0, 2.61, 37.84),
            listOf(1.35, 41.0, 4.45, 1482.0, 2.47, 37.85),
            listOf(1.71, 42.0, 4.47, 987.0, 2.65, 37.85),
            listOf(1.72, 52.0, 5.09, 1015.0, 2.14, 37.84),
            listOf(2.18, 52.0, 5.19, 853.0, 2.52, 37.84),
            listOf(2.60, 52.0, 5.27, 1006.0, 2.57, 37.84),
            listOf(2.40, 41.0, 4.49, 313.0, 2.54, 37.85),
            listOf(2.45, 49.0, 4.71, 607.0, 2.38, 37.85),
            listOf(1.80, 52.0, 4.78, 1102.0, 2.24, 37.85),
            listOf(5.07, 50.0, 6.42, 1131.0, 3.42, 37.84),
            listOf(4.07, 52.0, 5.76, 1258.0, 2.57, 37.84),
            listOf(3.53, 52.0, 5.04, 1152.0, 2.39, 37.84),
            listOf(3.14, 52.0, 4.73, 1162.0, 2.35, 37.84),
            listOf(2.81, 52.0, 4.78, 1262.0, 2.51, 37.85),
            listOf(1.37, 49.0, 3.48, 1139.0, 2.28, 37.85),
            listOf(1.05, 52.0, 3.73, 1262.0, 2.51, 37.85),
            listOf(1.48, 49.0, 4.67, 983.0, 2.56, 37.85),
            listOf(1.09, 48.0, 4.80, 1144.0, 2.51, 37.85),
            listOf(1.39, 52.0, 3.99, 901.0, 2.45, 37.85),
            listOf(1.23, 52.0, 4.70, 689.0, 2.09, 37.85),
            listOf(2.05, 52.0, 4.76, 904.0, 2.37, 37.85)
        )
        val targets = listOf(
            4.52, 3.58, 3.52, 3.41, 3.42, 2.69, 2.99, 2.41, 2.26, 2.61,
            2.81, 2.41, 2.13, 1.91, 1.59, 1.40, 1.52, 1.55, 1.58, 1.62,
            1.59, 1.13, 1.13, 0.99, 1.07, 1.07, 1.05, 1.08, 3.53, 2.98,
            2.42, 2.23, 1.83, 1.04, 1.05, 1.03, 1.04, 0.89, 0.88, 1.12
        )
        val featureNames = listOf("Median Income ($10k)", "House Age", "Avg Rooms", "Population", "Avg Occupants", "Latitude")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "median_income" to row[0].toString(),
                "house_age" to row[1].toInt().toString(),
                "avg_rooms" to row[2].toString(),
                "population" to row[3].toInt().toString(),
                "avg_occupancy" to row[4].toString(),
                "latitude" to row[5].toString(),
                "median_house_value" to "$${String.format("%.2f", targets[idx] * 100)}k"
            )
        }

        Dataset(
            id = "ds_california_housing",
            title = "California Real Estate Price Dataset",
            description = "High-dimensional regression dataset predicting median residential home values using demographic and geographical indicators.",
            category = DatasetCategory.TABULAR,
            taskType = TaskType.REGRESSION,
            samplesCount = features.size,
            featuresCount = 6,
            targetColumn = "median_house_value",
            difficulty = "Intermediate",
            tags = listOf("Regression", "Real Estate", "Economics", "Continuous Prediction"),
            featureNames = featureNames,
            classLabels = emptyList(),
            columnStats = listOf(
                ColumnStat("median_income", DataType.NUMERIC, false, 1.05, 8.32, 3.12, 1.74),
                ColumnStat("house_age", DataType.NUMERIC, false, 21.0, 52.0, 48.6, 6.2),
                ColumnStat("avg_rooms", DataType.NUMERIC, false, 3.48, 8.28, 5.08, 0.98),
                ColumnStat("population", DataType.NUMERIC, false, 313.0, 2401.0, 998.0, 390.0),
                ColumnStat("avg_occupancy", DataType.NUMERIC, false, 1.78, 3.42, 2.37, 0.31),
                ColumnStat("latitude", DataType.NUMERIC, false, 37.84, 37.88, 37.85, 0.01),
                ColumnStat("median_house_value", DataType.NUMERIC, true, 0.88, 4.52, 2.05, 0.96)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.88,
            downloadsCount = 11200
        )
    }

    // 4. Customer Churn & Retention (Enterprise SaaS)
    val customerChurnDataset: Dataset by lazy {
        val features = listOf(
            listOf(29.85, 1.0, 0.0, 0.0, 1.0),
            listOf(56.95, 34.0, 1.0, 0.0, 0.0),
            listOf(53.85, 2.0, 1.0, 0.0, 1.0),
            listOf(42.30, 45.0, 0.0, 0.0, 0.0),
            listOf(70.70, 2.0, 1.0, 1.0, 1.0),
            listOf(99.65, 8.0, 1.0, 1.0, 1.0),
            listOf(89.10, 22.0, 1.0, 0.0, 0.0),
            listOf(29.75, 10.0, 0.0, 0.0, 0.0),
            listOf(104.80, 28.0, 1.0, 1.0, 1.0),
            listOf(56.15, 62.0, 1.0, 0.0, 0.0),
            listOf(49.95, 13.0, 1.0, 0.0, 0.0),
            listOf(18.95, 16.0, 0.0, 0.0, 0.0),
            listOf(100.35, 58.0, 1.0, 1.0, 0.0),
            listOf(103.70, 49.0, 1.0, 1.0, 1.0),
            listOf(105.50, 25.0, 1.0, 1.0, 0.0),
            listOf(113.25, 69.0, 1.0, 1.0, 0.0),
            listOf(20.65, 52.0, 0.0, 0.0, 0.0),
            listOf(106.70, 71.0, 1.0, 1.0, 0.0),
            listOf(55.20, 10.0, 1.0, 0.0, 1.0),
            listOf(90.05, 21.0, 1.0, 1.0, 0.0),
            // Churn Cases (Class 1)
            listOf(39.65, 1.0, 0.0, 1.0, 1.0),
            listOf(84.45, 12.0, 1.0, 1.0, 1.0),
            listOf(20.15, 1.0, 0.0, 0.0, 0.0),
            listOf(73.90, 1.0, 1.0, 1.0, 1.0),
            listOf(98.00, 4.0, 1.0, 1.0, 1.0),
            listOf(85.80, 7.0, 1.0, 1.0, 1.0),
            listOf(95.45, 3.0, 1.0, 1.0, 1.0),
            listOf(74.40, 1.0, 1.0, 1.0, 1.0),
            listOf(99.00, 12.0, 1.0, 1.0, 1.0),
            listOf(102.95, 17.0, 1.0, 1.0, 1.0),
            listOf(79.85, 2.0, 1.0, 1.0, 1.0),
            listOf(90.85, 8.0, 1.0, 1.0, 1.0),
            listOf(74.80, 1.0, 1.0, 1.0, 1.0),
            listOf(94.40, 24.0, 1.0, 1.0, 1.0),
            listOf(89.85, 15.0, 1.0, 1.0, 1.0),
            listOf(69.65, 2.0, 1.0, 0.0, 1.0),
            listOf(70.15, 3.0, 1.0, 0.0, 1.0),
            listOf(95.00, 9.0, 1.0, 1.0, 1.0),
            listOf(79.20, 6.0, 1.0, 0.0, 1.0),
            listOf(84.30, 4.0, 1.0, 1.0, 1.0)
        )
        val targets = MutableList(20) { 0.0 } + MutableList(20) { 1.0 }
        val classLabels = listOf("Retained", "Churned")
        val featureNames = listOf("Monthly Charges ($)", "Tenure (Months)", "Has Fiber Optic", "Paperless Billing", "Senior Citizen")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "monthly_charges" to "$${row[0]}",
                "tenure_months" to row[1].toInt().toString(),
                "fiber_optic" to if (row[2] > 0.5) "Yes" else "No",
                "paperless" to if (row[3] > 0.5) "Yes" else "No",
                "senior_citizen" to if (row[4] > 0.5) "Yes" else "No",
                "churn_status" to classLabels[targets[idx].toInt()]
            )
        }

        Dataset(
            id = "ds_customer_churn",
            title = "Telecom Customer Churn Predictor",
            description = "Customer subscription and usage metrics dataset for building retention classification models and churn warning triggers.",
            category = DatasetCategory.TABULAR,
            taskType = TaskType.CLASSIFICATION,
            samplesCount = features.size,
            featuresCount = 5,
            targetColumn = "churn_status",
            difficulty = "Beginner",
            tags = listOf("SaaS", "Churn", "Business Intelligence", "Retention"),
            classLabels = classLabels,
            featureNames = featureNames,
            columnStats = listOf(
                ColumnStat("monthly_charges", DataType.NUMERIC, false, 18.95, 113.25, 71.4, 27.5),
                ColumnStat("tenure_months", DataType.NUMERIC, false, 1.0, 71.0, 20.3, 21.8),
                ColumnStat("fiber_optic", DataType.BOOLEAN, false, 0.0, 1.0, 0.78, 0.42),
                ColumnStat("paperless", DataType.BOOLEAN, false, 0.0, 1.0, 0.62, 0.49),
                ColumnStat("senior_citizen", DataType.BOOLEAN, false, 0.0, 1.0, 0.52, 0.50),
                ColumnStat("churn_status", DataType.CATEGORICAL, true, uniqueValues = classLabels)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.82,
            downloadsCount = 5710
        )
    }

    // 5. Wine Quality Grader
    val wineQualityDataset: Dataset by lazy {
        val features = listOf(
            listOf(7.4, 0.70, 0.00, 1.9, 0.076, 11.0, 34.0, 9.4),
            listOf(7.8, 0.88, 0.00, 2.6, 0.098, 25.0, 67.0, 9.8),
            listOf(7.8, 0.76, 0.04, 2.3, 0.092, 15.0, 54.0, 9.8),
            listOf(11.2, 0.28, 0.56, 1.9, 0.075, 17.0, 60.0, 9.8),
            listOf(7.4, 0.66, 0.00, 1.8, 0.075, 13.0, 40.0, 9.4),
            listOf(7.9, 0.60, 0.06, 1.6, 0.069, 15.0, 59.0, 9.4),
            listOf(7.3, 0.65, 0.00, 1.2, 0.065, 15.0, 21.0, 10.0),
            listOf(7.8, 0.58, 0.02, 2.0, 0.073, 9.0, 18.0, 9.5),
            listOf(7.5, 0.50, 0.36, 6.1, 0.071, 17.0, 102.0, 10.5),
            listOf(6.7, 0.58, 0.08, 1.8, 0.097, 15.0, 65.0, 9.2),
            // Premium Grade (Class 1)
            listOf(8.5, 0.28, 0.56, 1.8, 0.092, 35.0, 103.0, 10.5),
            listOf(8.1, 0.56, 0.28, 1.7, 0.368, 16.0, 56.0, 9.3),
            listOf(7.4, 0.59, 0.08, 4.4, 0.086, 6.0, 29.0, 12.8),
            listOf(7.9, 0.32, 0.51, 1.8, 0.341, 17.0, 56.0, 12.3),
            listOf(8.9, 0.22, 0.48, 1.8, 0.077, 29.0, 60.0, 11.9),
            listOf(7.6, 0.39, 0.31, 2.3, 0.082, 23.0, 71.0, 12.5),
            listOf(7.9, 0.35, 0.46, 3.6, 0.078, 15.0, 37.0, 10.7),
            listOf(7.7, 0.64, 0.21, 2.2, 0.077, 32.0, 133.0, 11.0),
            listOf(8.9, 0.84, 0.34, 1.4, 0.050, 4.0, 14.0, 10.4),
            listOf(8.5, 0.49, 0.11, 2.3, 0.084, 9.0, 67.0, 10.5)
        )
        val targets = MutableList(10) { 0.0 } + MutableList(10) { 1.0 }
        val classLabels = listOf("Standard Quality", "Premium Reserve")
        val featureNames = listOf("Fixed Acidity", "Volatile Acidity", "Citric Acid", "Residual Sugar", "Chlorides", "Free SO2", "Total SO2", "Alcohol %")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "fixed_acidity" to row[0].toString(),
                "volatile_acidity" to row[1].toString(),
                "citric_acid" to row[2].toString(),
                "residual_sugar" to row[3].toString(),
                "alcohol_pct" to "${row[7]}%",
                "quality_tier" to classLabels[targets[idx].toInt()]
            )
        }

        Dataset(
            id = "ds_wine_quality",
            title = "Enological Wine Chemical Profiling",
            description = "Physicochemical wine analysis parameters for sensory quality grading and fermentation consistency.",
            category = DatasetCategory.TABULAR,
            taskType = TaskType.CLASSIFICATION,
            samplesCount = features.size,
            featuresCount = 8,
            targetColumn = "quality_tier",
            difficulty = "Beginner",
            tags = listOf("Chemistry", "Sensory", "Food Tech", "Classification"),
            classLabels = classLabels,
            featureNames = featureNames,
            columnStats = listOf(
                ColumnStat("fixed_acidity", DataType.NUMERIC, false, 6.7, 11.2, 7.94, 0.95),
                ColumnStat("volatile_acidity", DataType.NUMERIC, false, 0.22, 0.88, 0.54, 0.18),
                ColumnStat("citric_acid", DataType.NUMERIC, false, 0.00, 0.56, 0.22, 0.19),
                ColumnStat("alcohol_pct", DataType.NUMERIC, false, 9.2, 12.8, 10.55, 1.15),
                ColumnStat("quality_tier", DataType.CATEGORICAL, true, uniqueValues = classLabels)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.75,
            downloadsCount = 4230
        )
    }

    // 6. Defect Inspection Vision Features (Computer Vision extracted embedding features)
    val defectInspectionDataset: Dataset by lazy {
        val features = listOf(
            listOf(0.12, 0.88, 0.05, 0.94, 0.02),
            listOf(0.15, 0.84, 0.07, 0.91, 0.04),
            listOf(0.09, 0.92, 0.03, 0.97, 0.01),
            listOf(0.14, 0.86, 0.06, 0.93, 0.03),
            listOf(0.11, 0.89, 0.04, 0.95, 0.02),
            listOf(0.18, 0.81, 0.08, 0.89, 0.05),
            listOf(0.10, 0.90, 0.05, 0.96, 0.02),
            listOf(0.13, 0.87, 0.06, 0.92, 0.03),
            listOf(0.16, 0.83, 0.07, 0.90, 0.04),
            listOf(0.08, 0.94, 0.02, 0.98, 0.01),
            // Defect Samples (Class 1)
            listOf(0.78, 0.24, 0.65, 0.31, 0.58),
            listOf(0.85, 0.19, 0.72, 0.28, 0.64),
            listOf(0.69, 0.31, 0.58, 0.42, 0.49),
            listOf(0.92, 0.12, 0.81, 0.19, 0.75),
            listOf(0.74, 0.28, 0.61, 0.35, 0.52),
            listOf(0.81, 0.22, 0.68, 0.25, 0.61),
            listOf(0.64, 0.36, 0.53, 0.46, 0.43),
            listOf(0.88, 0.15, 0.76, 0.21, 0.70),
            listOf(0.76, 0.26, 0.63, 0.33, 0.55),
            listOf(0.90, 0.14, 0.79, 0.18, 0.72)
        )
        val targets = MutableList(10) { 0.0 } + MutableList(10) { 1.0 }
        val classLabels = listOf("Passed QA (Normal)", "Defective (Scratch / Crack)")
        val featureNames = listOf("Edge Gradient Anomaly", "Surface Uniformity", "Color Variance Index", "Structural Symmetry", "Specular Roughness")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "edge_anomaly" to String.format("%.3f", row[0]),
                "uniformity" to String.format("%.3f", row[1]),
                "color_var" to String.format("%.3f", row[2]),
                "symmetry" to String.format("%.3f", row[3]),
                "roughness" to String.format("%.3f", row[4]),
                "inspection_status" to classLabels[targets[idx].toInt()]
            )
        }

        Dataset(
            id = "ds_defect_vision",
            title = "Industrial Vision Defect Detection",
            description = "High-precision computer vision extracted morphological texture descriptors for automated factory optical inspection.",
            category = DatasetCategory.VISION,
            taskType = TaskType.FEATURE_DETECTION,
            samplesCount = features.size,
            featuresCount = 5,
            targetColumn = "inspection_status",
            difficulty = "Advanced",
            tags = listOf("Computer Vision", "Manufacturing", "Quality Assurance", "Anomaly Detection"),
            classLabels = classLabels,
            featureNames = featureNames,
            columnStats = listOf(
                ColumnStat("edge_anomaly", DataType.NUMERIC, false, 0.08, 0.92, 0.46, 0.36),
                ColumnStat("uniformity", DataType.NUMERIC, false, 0.12, 0.94, 0.55, 0.33),
                ColumnStat("inspection_status", DataType.CATEGORICAL, true, uniqueValues = classLabels)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.92,
            downloadsCount = 7890
        )
    }

    // 7. Solar Power Grid Time-Series Output (Regression)
    val solarPowerDataset: Dataset by lazy {
        val features = listOf(
            listOf(22.4, 45.0, 2.1, 750.0, 11.0),
            listOf(25.1, 40.0, 3.2, 880.0, 12.0),
            listOf(27.8, 38.0, 4.0, 940.0, 13.0),
            listOf(28.2, 35.0, 4.5, 910.0, 14.0),
            listOf(26.5, 39.0, 3.8, 790.0, 15.0),
            listOf(23.1, 48.0, 2.4, 520.0, 16.0),
            listOf(19.8, 55.0, 1.8, 280.0, 17.0),
            listOf(17.2, 62.0, 1.2, 80.0, 18.0),
            listOf(15.0, 70.0, 0.8, 0.0, 19.0),
            listOf(14.2, 75.0, 0.5, 0.0, 20.0),
            listOf(16.5, 68.0, 1.5, 120.0, 7.0),
            listOf(18.9, 58.0, 2.0, 340.0, 8.0),
            listOf(21.4, 50.0, 2.8, 580.0, 9.0),
            listOf(24.0, 43.0, 3.5, 780.0, 10.0),
            listOf(26.7, 37.0, 4.2, 920.0, 11.5),
            listOf(28.0, 33.0, 4.8, 960.0, 12.5),
            listOf(27.5, 36.0, 4.4, 890.0, 13.5),
            listOf(25.0, 42.0, 3.1, 680.0, 14.5),
            listOf(22.0, 51.0, 2.2, 410.0, 15.5),
            listOf(18.5, 60.0, 1.4, 190.0, 16.5)
        )
        val targets = listOf(
            38.5, 46.2, 51.0, 49.8, 41.5, 26.8, 14.2, 3.9, 0.0, 0.0,
            5.8, 17.4, 29.8, 41.0, 49.5, 52.8, 48.2, 35.4, 21.0, 9.5
        )
        val featureNames = listOf("Ambient Temp (°C)", "Relative Humidity (%)", "Wind Speed (m/s)", "Solar Irradiance (W/m²)", "Hour of Day")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "temp" to "${row[0]}°C",
                "humidity" to "${row[1].toInt()}%",
                "wind" to "${row[2]}m/s",
                "irradiance" to "${row[3].toInt()} W/m²",
                "hour" to "${row[4]}:00",
                "power_output_kw" to "${targets[idx]} kW"
            )
        }

        Dataset(
            id = "ds_solar_power",
            title = "Photovoltaic Solar Generation Array",
            description = "Time-series meteorological sensor parameters to forecast instantaneous inverter photovoltaic power output.",
            category = DatasetCategory.TIME_SERIES,
            taskType = TaskType.REGRESSION,
            samplesCount = features.size,
            featuresCount = 5,
            targetColumn = "power_output_kw",
            difficulty = "Intermediate",
            tags = listOf("Renewable Energy", "Solar", "Time-Series", "Regression"),
            featureNames = featureNames,
            classLabels = emptyList(),
            columnStats = listOf(
                ColumnStat("temp", DataType.NUMERIC, false, 14.2, 28.2, 21.8, 4.8),
                ColumnStat("humidity", DataType.NUMERIC, false, 33.0, 75.0, 49.6, 12.8),
                ColumnStat("irradiance", DataType.NUMERIC, false, 0.0, 960.0, 528.0, 334.0),
                ColumnStat("power_output_kw", DataType.NUMERIC, true, 0.0, 52.8, 27.4, 18.2)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.85,
            downloadsCount = 5120
        )
    }

    // 8. Cyber Security Intrusion Detection
    val cyberSecurityDataset: Dataset by lazy {
        val features = listOf(
            listOf(64.0, 0.02, 12.0, 0.0, 1.0),
            listOf(128.0, 0.05, 18.0, 0.0, 1.0),
            listOf(256.0, 0.08, 24.0, 0.0, 1.0),
            listOf(512.0, 0.12, 32.0, 0.0, 1.0),
            listOf(64.0, 0.01, 8.0, 0.0, 1.0),
            listOf(1024.0, 0.25, 45.0, 0.0, 1.0),
            listOf(128.0, 0.03, 14.0, 0.0, 1.0),
            listOf(512.0, 0.10, 28.0, 0.0, 1.0),
            // Attacks: SYN Flood / Port Scan (Class 1)
            listOf(40.0, 0.001, 850.0, 0.95, 0.0),
            listOf(40.0, 0.002, 920.0, 0.98, 0.0),
            listOf(48.0, 0.001, 1100.0, 0.99, 0.0),
            listOf(32.0, 0.003, 780.0, 0.92, 0.0),
            listOf(40.0, 0.001, 1250.0, 0.99, 0.0),
            listOf(44.0, 0.002, 890.0, 0.94, 0.0)
        )
        val targets = MutableList(8) { 0.0 } + MutableList(6) { 1.0 }
        val classLabels = listOf("Benign Traffic", "DDoS / SYN Flood Attack")
        val featureNames = listOf("Packet Size (Bytes)", "Flow Duration (s)", "Packets Per Sec", "Error Ratio", "TCP Handshake Complete")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "packet_size" to "${row[0].toInt()} B",
                "duration" to "${row[1]}s",
                "packets_per_sec" to row[2].toInt().toString(),
                "error_ratio" to row[3].toString(),
                "handshake" to if (row[4] > 0.5) "Yes" else "No",
                "traffic_type" to classLabels[targets[idx].toInt()]
            )
        }

        Dataset(
            id = "ds_cyber_intrusion",
            title = "Network Intrusion & Cyber Defense",
            description = "Packet flow signatures and connection telemetry for classifying DDoS assaults, SYN floods, and legitimate traffic.",
            category = DatasetCategory.SENSOR,
            taskType = TaskType.CLASSIFICATION,
            samplesCount = features.size,
            featuresCount = 5,
            targetColumn = "traffic_type",
            difficulty = "Intermediate",
            tags = listOf("Cybersecurity", "Networking", "DDoS", "Intrusion Detection"),
            classLabels = classLabels,
            featureNames = featureNames,
            columnStats = listOf(
                ColumnStat("packet_size", DataType.NUMERIC, false, 32.0, 1024.0, 206.0, 290.0),
                ColumnStat("packets_per_sec", DataType.NUMERIC, false, 8.0, 1250.0, 427.0, 480.0),
                ColumnStat("traffic_type", DataType.CATEGORICAL, true, uniqueValues = classLabels)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.90,
            downloadsCount = 6800
        )
    }

    // 9. NLP Sentiment Corpus
    val sentimentNlpDataset: Dataset by lazy {
        val features = listOf(
            listOf(0.92, 0.05, 12.0, 0.88),
            listOf(0.85, 0.08, 18.0, 0.79),
            listOf(0.96, 0.02, 9.0, 0.94),
            listOf(0.78, 0.12, 24.0, 0.71),
            listOf(0.90, 0.04, 15.0, 0.85),
            // Negative Sentiment (Class 1)
            listOf(0.08, 0.91, 14.0, 0.12),
            listOf(0.05, 0.95, 20.0, 0.08),
            listOf(0.12, 0.84, 11.0, 0.18),
            listOf(0.03, 0.98, 16.0, 0.04),
            listOf(0.15, 0.82, 28.0, 0.21)
        )
        val targets = MutableList(5) { 0.0 } + MutableList(5) { 1.0 }
        val classLabels = listOf("Positive", "Negative")
        val featureNames = listOf("Positive Lexicon Density", "Negative Lexicon Density", "Token Count", "VADER Polarity Compound")

        val sampleRows = listOf(
            mapOf("review" to "Super fast inference and crystal clear documentation!", "tokens" to "12", "sentiment" to "Positive"),
            mapOf("review" to "Exceptional accuracy on our benchmark dataset.", "tokens" to "18", "sentiment" to "Positive"),
            mapOf("review" to "Loved the intuitive API and clean architecture.", "tokens" to "9", "sentiment" to "Positive"),
            mapOf("review" to "Great convergence speed with zero memory leaks.", "tokens" to "24", "sentiment" to "Positive"),
            mapOf("review" to "Highly recommended for production deployment.", "tokens" to "15", "sentiment" to "Positive"),
            mapOf("review" to "Severe overfitting and constant gradient explosion.", "tokens" to "14", "sentiment" to "Negative"),
            mapOf("review" to "Model crashed after 2 epochs due to invalid shapes.", "tokens" to "20", "sentiment" to "Negative"),
            mapOf("review" to "Poor accuracy and missing feature columns.", "tokens" to "11", "sentiment" to "Negative"),
            mapOf("review" to "Horrible latency on mobile devices, unusable.", "tokens" to "16", "sentiment" to "Negative"),
            mapOf("review" to "Unstable loss curves and corrupted validation split.", "tokens" to "28", "sentiment" to "Negative")
        )

        Dataset(
            id = "ds_sentiment_nlp",
            title = "Customer Feedback Sentiment NLP",
            description = "Natural language textual token features and sentiment embeddings for binary emotional polarity classification.",
            category = DatasetCategory.NLP,
            taskType = TaskType.SENTIMENT,
            samplesCount = features.size,
            featuresCount = 4,
            targetColumn = "sentiment",
            difficulty = "Beginner",
            tags = listOf("NLP", "Text", "Sentiment", "Customer Feedback"),
            classLabels = classLabels,
            featureNames = featureNames,
            columnStats = listOf(
                ColumnStat("review", DataType.TEXT, false),
                ColumnStat("tokens", DataType.NUMERIC, false, 9.0, 28.0, 16.7, 5.8),
                ColumnStat("sentiment", DataType.CATEGORICAL, true, uniqueValues = classLabels)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.86,
            downloadsCount = 7120
        )
    }

    // 10. Audio Keyword Spotting Features
    val audioKeywordDataset: Dataset by lazy {
        val features = listOf(
            listOf(14.2, 8.5, 1200.0, 0.15),
            listOf(15.1, 7.8, 1250.0, 0.16),
            listOf(13.8, 9.1, 1180.0, 0.14),
            listOf(14.9, 8.2, 1220.0, 0.15),
            // Keyword "Stop" (Class 1)
            listOf(8.4, 16.2, 2100.0, 0.28),
            listOf(7.9, 17.0, 2150.0, 0.30),
            listOf(8.8, 15.8, 2050.0, 0.26),
            listOf(8.1, 16.5, 2120.0, 0.29),
            // Keyword "Next" (Class 2)
            listOf(11.2, 12.4, 1650.0, 0.21),
            listOf(10.8, 13.1, 1700.0, 0.23),
            listOf(11.9, 11.8, 1600.0, 0.20),
            listOf(11.4, 12.6, 1680.0, 0.22)
        )
        val targets = MutableList(4) { 0.0 } + MutableList(4) { 1.0 } + MutableList(4) { 2.0 }
        val classLabels = listOf("Command: START", "Command: STOP", "Command: NEXT")
        val featureNames = listOf("MFCC Coeff 1", "MFCC Coeff 2", "Spectral Centroid (Hz)", "Zero Crossing Rate")

        val sampleRows = features.mapIndexed { idx, row ->
            mapOf(
                "mfcc_1" to row[0].toString(),
                "mfcc_2" to row[1].toString(),
                "spectral_centroid" to "${row[2].toInt()} Hz",
                "zcr" to row[3].toString(),
                "command" to classLabels[targets[idx].toInt()]
            )
        }

        Dataset(
            id = "ds_audio_keyword",
            title = "Acoustic Keyword Spotting DSP",
            description = "Spectral frequency acoustic descriptors (MFCCs, spectral centroid, ZCR) for embedded voice trigger detection.",
            category = DatasetCategory.AUDIO,
            taskType = TaskType.CLASSIFICATION,
            samplesCount = features.size,
            featuresCount = 4,
            targetColumn = "command",
            difficulty = "Advanced",
            tags = listOf("Audio DSP", "Speech", "MFCC", "Voice Trigger"),
            classLabels = classLabels,
            featureNames = featureNames,
            columnStats = listOf(
                ColumnStat("spectral_centroid", DataType.NUMERIC, false, 1180.0, 2150.0, 1658.0, 390.0),
                ColumnStat("command", DataType.CATEGORICAL, true, uniqueValues = classLabels)
            ),
            sampleRows = sampleRows,
            numericFeatures = features,
            numericTargets = targets,
            rating = 4.89,
            downloadsCount = 4900
        )
    }
}
