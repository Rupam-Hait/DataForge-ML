package com.example.data.model

enum class MLModelType(
    val title: String,
    val description: String,
    val supportedTasks: List<TaskType>
) {
    NEURAL_NETWORK_MLP(
        title = "Multi-Layer Perceptron (MLP)",
        description = "Deep feed-forward neural network with configurable hidden units, backpropagation, and non-linear activations.",
        supportedTasks = listOf(TaskType.CLASSIFICATION, TaskType.REGRESSION, TaskType.SENTIMENT, TaskType.FEATURE_DETECTION)
    ),
    RANDOM_FOREST(
        title = "Random Forest Ensemble",
        description = "Ensemble of randomized decision trees with bootstrap aggregation, out-of-bag validation, and Gini feature importance.",
        supportedTasks = listOf(TaskType.CLASSIFICATION, TaskType.FEATURE_DETECTION)
    ),
    DECISION_TREE(
        title = "Decision Tree (CART)",
        description = "Recursive greedy binary splitting tree using Gini impurity or Information Gain to construct transparent decision rules.",
        supportedTasks = listOf(TaskType.CLASSIFICATION, TaskType.FEATURE_DETECTION)
    ),
    K_NEAREST_NEIGHBORS(
        title = "K-Nearest Neighbors (KNN)",
        description = "Instance-based non-parametric algorithm computing Euclidean/Manhattan distance matrices for majority class voting.",
        supportedTasks = listOf(TaskType.CLASSIFICATION, TaskType.SENTIMENT)
    ),
    LOGISTIC_REGRESSION(
        title = "Multinomial Logistic Regression",
        description = "Linear classifier using softmax activation and cross-entropy loss with L1/L2 regularized gradient descent.",
        supportedTasks = listOf(TaskType.CLASSIFICATION, TaskType.SENTIMENT)
    ),
    LINEAR_REGRESSION(
        title = "Ridge / Linear Regression",
        description = "Continuous value predictor optimizing Mean Squared Error with L2 Tikhonov regularization.",
        supportedTasks = listOf(TaskType.REGRESSION)
    ),
    K_MEANS_CLUSTERING(
        title = "K-Means Clustering",
        description = "Unsupervised clustering partitioning multidimensional vectors into k distinct cluster centroids via Lloyd's algorithm.",
        supportedTasks = listOf(TaskType.CLUSTERING, TaskType.CLASSIFICATION)
    )
}

enum class ActivationFunction(val displayName: String) {
    RELU("ReLU"),
    SIGMOID("Sigmoid"),
    TANH("Tanh"),
    LEAKY_RELU("Leaky ReLU")
}

enum class OptimizerType(val displayName: String) {
    ADAM("Adam (Adaptive Moments)"),
    RMSPROP("RMSProp"),
    MOMENTUM_SGD("SGD + Momentum (0.9)"),
    STANDARD_SGD("Standard SGD")
}

enum class DistanceMetric(val displayName: String) {
    EUCLIDEAN("Euclidean Distance (L2)"),
    MANHATTAN("Manhattan Distance (L1)")
}

data class Hyperparameters(
    val modelType: MLModelType = MLModelType.NEURAL_NETWORK_MLP,
    val learningRate: Double = 0.05,
    val epochs: Int = 30,
    val batchSize: Int = 16,
    val hiddenUnits: Int = 16,
    val hiddenLayers: Int = 2,
    val activation: ActivationFunction = ActivationFunction.RELU,
    val optimizer: OptimizerType = OptimizerType.ADAM,
    val weightDecayL2: Double = 0.0001,
    val dropoutRate: Float = 0.10f,
    val testSplitRatio: Float = 0.2f,
    val kNeighbors: Int = 5,
    val distanceMetric: DistanceMetric = DistanceMetric.EUCLIDEAN,
    val maxTreeDepth: Int = 4,
    val nTrees: Int = 15,
    val nClusters: Int = 3,
    val normalizeFeatures: Boolean = true
)

data class EpochMetric(
    val epoch: Int,
    val trainLoss: Double,
    val valLoss: Double,
    val trainAccuracy: Double,
    val valAccuracy: Double
)

data class FeatureImportance(
    val featureName: String,
    val importanceScore: Double,
    val rank: Int = 0
)

data class DecisionBoundaryPoint(
    val x: Double,
    val y: Double,
    val predictedClass: Int,
    val confidence: Double
)

data class DecisionTreeRule(
    val featureName: String,
    val threshold: Double,
    val leftLabel: String,
    val rightLabel: String,
    val depth: Int
)

data class TrainingProgressState(
    val isTraining: Boolean = false,
    val isCompleted: Boolean = false,
    val currentEpoch: Int = 0,
    val totalEpochs: Int = 30,
    val currentTrainLoss: Double = 0.0,
    val currentValLoss: Double = 0.0,
    val currentTrainAcc: Double = 0.0,
    val currentValAcc: Double = 0.0,
    val r2Score: Double = 0.0, // For regression
    val mseScore: Double = 0.0, // For regression
    val metricsHistory: List<EpochMetric> = emptyList(),
    val confusionMatrix: List<List<Int>> = emptyList(),
    val classLabels: List<String> = emptyList(),
    val featureImportances: List<FeatureImportance> = emptyList(),
    val treeRules: List<DecisionTreeRule> = emptyList(),
    val clusterCentroids: List<List<Double>> = emptyList(),
    val boundaryGrid: List<DecisionBoundaryPoint> = emptyList(),
    val trainedWeightsInfo: String = "",
    val trainingTimeMs: Long = 0,
    val modelSummary: String = ""
)

data class PredictionResult(
    val predictedClass: String = "",
    val predictedClassIndex: Int = 0,
    val predictedContinuousValue: Double = 0.0,
    val classProbabilities: List<Pair<String, Double>> = emptyList(),
    val confidence: Double = 0.0,
    val explanation: String = "",
    val nearestNeighborsInfo: List<String> = emptyList(),
    val activatedPath: String = ""
)
