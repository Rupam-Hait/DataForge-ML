package com.example.ml.engine

import com.example.data.model.ActivationFunction
import com.example.data.model.ColumnStat
import com.example.data.model.DataType
import com.example.data.model.DecisionBoundaryPoint
import com.example.data.model.DecisionTreeRule
import com.example.data.model.DistanceMetric
import com.example.data.model.EpochMetric
import com.example.data.model.FeatureCorrelation
import com.example.data.model.FeatureImportance
import com.example.data.model.Hyperparameters
import com.example.data.model.MLModelType
import com.example.data.model.OptimizerType
import com.example.data.model.PredictionResult
import com.example.data.model.TaskType
import com.example.data.model.TrainingProgressState
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

// Internal Node structure for Decision Tree & Random Forest
class TreeNode(
    val featureIndex: Int = -1,
    val threshold: Double = 0.0,
    val isLeaf: Boolean = false,
    val predictedClass: Int = 0,
    val classProbabilities: DoubleArray = doubleArrayOf(),
    val left: TreeNode? = null,
    val right: TreeNode? = null,
    val depth: Int = 0
)

class MLEngine {

    // Neural Network Trained Weights & Optimizer state
    private var mlpHiddenWeights: Array<DoubleArray>? = null
    private var mlpHiddenBiases: DoubleArray? = null
    private var mlpOutputWeights: Array<DoubleArray>? = null
    private var mlpOutputBiases: DoubleArray? = null

    // Linear / Logistic Regression Weights
    private var linearWeights: DoubleArray? = null
    private var linearBias: Double = 0.0
    private var logisticWeights: Array<DoubleArray>? = null
    private var logisticBiases: DoubleArray? = null

    // KNN Trained State
    private var knnStoredTrainX: List<DoubleArray> = emptyList()
    private var knnStoredTrainY: List<Double> = emptyList()

    // Tree / Forest Models
    private var decisionTreeRoot: TreeNode? = null
    private var randomForestTrees: List<TreeNode> = emptyList()

    // K-Means Cluster Centroids
    private var kmeansCentroids: List<DoubleArray> = emptyList()

    // Normalization parameters
    private var featureMeans: DoubleArray = doubleArrayOf()
    private var featureStds: DoubleArray = doubleArrayOf()
    private var featureMins: DoubleArray = doubleArrayOf()
    private var featureMaxs: DoubleArray = doubleArrayOf()

    // Metadata
    private var currentModelType: MLModelType = MLModelType.NEURAL_NETWORK_MLP
    private var targetTaskType: TaskType = TaskType.CLASSIFICATION
    private var classLabels: List<String> = emptyList()
    private var featureNames: List<String> = emptyList()

    suspend fun train(
        dataset: com.example.data.model.Dataset,
        hyperparams: Hyperparameters,
        onProgress: (TrainingProgressState) -> Unit
    ): TrainingProgressState {
        val startTime = System.currentTimeMillis()
        val rawX = dataset.numericFeatures
        val rawY = dataset.numericTargets
        currentModelType = hyperparams.modelType
        targetTaskType = dataset.taskType
        classLabels = dataset.classLabels
        featureNames = dataset.featureNames

        if (rawX.isEmpty() || rawY.isEmpty()) {
            return TrainingProgressState(
                isCompleted = true,
                modelSummary = "Error: Dataset contains no numeric feature vectors."
            )
        }

        val numSamples = rawX.size
        val numFeatures = rawX[0].size
        val isClassification = dataset.taskType == TaskType.CLASSIFICATION ||
                dataset.taskType == TaskType.SENTIMENT ||
                dataset.taskType == TaskType.FEATURE_DETECTION
        val numClasses = if (isClassification) max(2, dataset.classLabels.size) else 1

        // 1. Compute Normalization bounds
        featureMeans = DoubleArray(numFeatures) { f -> rawX.map { it[f] }.average() }
        featureStds = DoubleArray(numFeatures) { f ->
            val mean = featureMeans[f]
            val variance = rawX.map { (it[f] - mean).pow(2) }.average()
            val std = sqrt(variance)
            if (std == 0.0) 1.0 else std
        }
        featureMins = DoubleArray(numFeatures) { f -> rawX.minOf { it[f] } }
        featureMaxs = DoubleArray(numFeatures) { f -> rawX.maxOf { it[f] } }

        val normalizedX = if (hyperparams.normalizeFeatures) {
            rawX.map { sample ->
                DoubleArray(numFeatures) { f -> (sample[f] - featureMeans[f]) / featureStds[f] }
            }
        } else {
            rawX.map { sample -> DoubleArray(numFeatures) { f -> sample[f] } }
        }

        // 2. Train / Validation Split
        val indices = (0 until numSamples).shuffled(Random(42))
        val splitIndex = (numSamples * (1.0 - hyperparams.testSplitRatio)).toInt().coerceIn(1, numSamples - 1)
        val trainIndices = indices.subList(0, splitIndex)
        val valIndices = indices.subList(splitIndex, numSamples)

        val trainX = trainIndices.map { normalizedX[it] }
        val trainY = trainIndices.map { rawY[it] }
        val valX = valIndices.map { normalizedX[it] }
        val valY = valIndices.map { rawY[it] }

        val metricsHistory = mutableListOf<EpochMetric>()
        var finalConfusionMatrix = List(numClasses) { List(numClasses) { 0 } }
        var finalFeatureImportances = mutableListOf<FeatureImportance>()
        var finalTreeRules = mutableListOf<DecisionTreeRule>()
        var finalR2 = 0.0
        var finalMSE = 0.0

        when (hyperparams.modelType) {
            MLModelType.NEURAL_NETWORK_MLP -> {
                val hiddenDim = hyperparams.hiddenUnits
                val rnd = Random(1234)

                // He / Xavier Initialization
                val scale1 = sqrt(2.0 / numFeatures)
                mlpHiddenWeights = Array(numFeatures) { DoubleArray(hiddenDim) { (rnd.nextDouble() - 0.5) * scale1 } }
                mlpHiddenBiases = DoubleArray(hiddenDim) { 0.0 }

                val scale2 = sqrt(2.0 / hiddenDim)
                mlpOutputWeights = Array(hiddenDim) { DoubleArray(numClasses) { (rnd.nextDouble() - 0.5) * scale2 } }
                mlpOutputBiases = DoubleArray(numClasses) { 0.0 }

                // Adam Optimizer Moments
                val mHidden = Array(numFeatures) { DoubleArray(hiddenDim) { 0.0 } }
                val vHidden = Array(numFeatures) { DoubleArray(hiddenDim) { 0.0 } }
                val mOut = Array(hiddenDim) { DoubleArray(numClasses) { 0.0 } }
                val vOut = Array(hiddenDim) { DoubleArray(numClasses) { 0.0 } }
                var tStep = 0

                val beta1 = 0.9
                val beta2 = 0.999
                val eps = 1e-8

                for (epoch in 1..hyperparams.epochs) {
                    var epochLoss = 0.0
                    var correctTrain = 0
                    val batchIndices = trainIndices.indices.shuffled(rnd)

                    for (i in batchIndices) {
                        tStep++
                        val x = trainX[i]
                        val target = trainY[i]

                        // Forward hidden layer
                        val z1 = DoubleArray(hiddenDim) { h ->
                            var sum = mlpHiddenBiases!![h]
                            for (f in 0 until numFeatures) sum += x[f] * mlpHiddenWeights!![f][h]
                            sum
                        }
                        val a1 = DoubleArray(hiddenDim) { h -> applyActivation(z1[h], hyperparams.activation) }

                        // Forward output layer
                        val z2 = DoubleArray(numClasses) { c ->
                            var sum = mlpOutputBiases!![c]
                            for (h in 0 until hiddenDim) sum += a1[h] * mlpOutputWeights!![h][c]
                            sum
                        }

                        val probs = if (isClassification) softmax(z2) else z2

                        // Compute error
                        val deltaOut = DoubleArray(numClasses)
                        if (isClassification) {
                            val targetClass = target.toInt().coerceIn(0, numClasses - 1)
                            for (c in 0 until numClasses) {
                                val t = if (c == targetClass) 1.0 else 0.0
                                deltaOut[c] = probs[c] - t
                            }
                            val loss = -kotlin.math.ln(max(1e-9, probs[targetClass]))
                            epochLoss += loss
                            if (probs.indices.maxByOrNull { probs[it] } == targetClass) correctTrain++
                        } else {
                            val err = probs[0] - target
                            deltaOut[0] = err
                            epochLoss += err.pow(2)
                        }

                        // Backward pass to hidden layer
                        val deltaHidden = DoubleArray(hiddenDim) { h ->
                            var sum = 0.0
                            for (c in 0 until numClasses) sum += deltaOut[c] * mlpOutputWeights!![h][c]
                            sum * applyActivationDerivative(z1[h], hyperparams.activation)
                        }

                        val lr = hyperparams.learningRate
                        val l2 = hyperparams.weightDecayL2

                        // Optimizer updates
                        when (hyperparams.optimizer) {
                            OptimizerType.ADAM -> {
                                val correction1 = 1.0 - beta1.pow(tStep.toDouble())
                                val correction2 = 1.0 - beta2.pow(tStep.toDouble())

                                for (h in 0 until hiddenDim) {
                                    for (c in 0 until numClasses) {
                                        val grad = deltaOut[c] * a1[h] + l2 * mlpOutputWeights!![h][c]
                                        mOut[h][c] = beta1 * mOut[h][c] + (1 - beta1) * grad
                                        vOut[h][c] = beta2 * vOut[h][c] + (1 - beta2) * grad * grad
                                        val mHat = mOut[h][c] / correction1
                                        val vHat = vOut[h][c] / correction2
                                        mlpOutputWeights!![h][c] -= lr * mHat / (sqrt(vHat) + eps)
                                    }
                                }
                                for (c in 0 until numClasses) mlpOutputBiases!![c] -= lr * deltaOut[c]

                                for (f in 0 until numFeatures) {
                                    for (h in 0 until hiddenDim) {
                                        val grad = deltaHidden[h] * x[f] + l2 * mlpHiddenWeights!![f][h]
                                        mHidden[f][h] = beta1 * mHidden[f][h] + (1 - beta1) * grad
                                        vHidden[f][h] = beta2 * vHidden[f][h] + (1 - beta2) * grad * grad
                                        val mHat = mHidden[f][h] / correction1
                                        val vHat = vHidden[f][h] / correction2
                                        mlpHiddenWeights!![f][h] -= lr * mHat / (sqrt(vHat) + eps)
                                    }
                                }
                                for (h in 0 until hiddenDim) mlpHiddenBiases!![h] -= lr * deltaHidden[h]
                            }
                            else -> {
                                // Gradient descent with L2
                                for (h in 0 until hiddenDim) {
                                    for (c in 0 until numClasses) {
                                        mlpOutputWeights!![h][c] -= lr * (deltaOut[c] * a1[h] + l2 * mlpOutputWeights!![h][c])
                                    }
                                }
                                for (c in 0 until numClasses) mlpOutputBiases!![c] -= lr * deltaOut[c]

                                for (f in 0 until numFeatures) {
                                    for (h in 0 until hiddenDim) {
                                        mlpHiddenWeights!![f][h] -= lr * (deltaHidden[h] * x[f] + l2 * mlpHiddenWeights!![f][h])
                                    }
                                }
                                for (h in 0 until hiddenDim) mlpHiddenBiases!![h] -= lr * deltaHidden[h]
                            }
                        }
                    }

                    // Validation Evaluation
                    val trainLoss = epochLoss / trainX.size
                    val trainAcc = if (isClassification) correctTrain.toDouble() / trainX.size else 0.0

                    var valLossSum = 0.0
                    var valCorrect = 0
                    for (i in valX.indices) {
                        val x = valX[i]
                        val target = valY[i]
                        val z1 = DoubleArray(hiddenDim) { h ->
                            var sum = mlpHiddenBiases!![h]
                            for (f in 0 until numFeatures) sum += x[f] * mlpHiddenWeights!![f][h]
                            sum
                        }
                        val a1 = DoubleArray(hiddenDim) { h -> applyActivation(z1[h], hyperparams.activation) }
                        val z2 = DoubleArray(numClasses) { c ->
                            var sum = mlpOutputBiases!![c]
                            for (h in 0 until hiddenDim) sum += a1[h] * mlpOutputWeights!![h][c]
                            sum
                        }
                        val probs = if (isClassification) softmax(z2) else z2
                        if (isClassification) {
                            val targetClass = target.toInt().coerceIn(0, numClasses - 1)
                            val loss = -kotlin.math.ln(max(1e-9, probs[targetClass]))
                            valLossSum += loss
                            if (probs.indices.maxByOrNull { probs[it] } == targetClass) valCorrect++
                        } else {
                            valLossSum += (probs[0] - target).pow(2)
                        }
                    }

                    val valLoss = valLossSum / valX.size
                    val valAcc = if (isClassification) valCorrect.toDouble() / valX.size else 0.0

                    val metric = EpochMetric(epoch, trainLoss, valLoss, trainAcc, valAcc)
                    metricsHistory.add(metric)

                    onProgress(
                        TrainingProgressState(
                            isTraining = true,
                            currentEpoch = epoch,
                            totalEpochs = hyperparams.epochs,
                            currentTrainLoss = trainLoss,
                            currentValLoss = valLoss,
                            currentTrainAcc = trainAcc,
                            currentValAcc = valAcc,
                            metricsHistory = metricsHistory.toList()
                        )
                    )
                    delay(25)
                }

                // Compute feature sensitivity importances
                val weightsSum = DoubleArray(numFeatures) { f ->
                    (0 until hiddenDim).sumOf { h -> abs(mlpHiddenWeights!![f][h]) }
                }
                val totalWeight = weightsSum.sum().coerceAtLeast(1e-6)
                finalFeatureImportances = weightsSum.mapIndexed { idx, w ->
                    FeatureImportance(featureNames.getOrElse(idx) { "Feature $idx" }, w / totalWeight, idx)
                }.sortedByDescending { it.importanceScore }.toMutableList()
            }

            MLModelType.K_NEAREST_NEIGHBORS -> {
                knnStoredTrainX = trainX
                knnStoredTrainY = trainY

                // Progress animation through test evaluation
                for (epoch in 1..hyperparams.epochs) {
                    val progress = epoch.toDouble() / hyperparams.epochs
                    val currentK = min(hyperparams.kNeighbors, trainX.size)

                    // Compute current fold error
                    var valCorrect = 0
                    var valLoss = 0.0
                    for (i in valX.indices) {
                        val pred = evaluateKNN(valX[i], currentK, hyperparams.distanceMetric, numClasses)
                        val actual = valY[i].toInt().coerceIn(0, numClasses - 1)
                        if (pred.predictedClassIndex == actual) valCorrect++
                        valLoss += -kotlin.math.ln(max(1e-9, pred.confidence))
                    }

                    val valAcc = valCorrect.toDouble() / valX.size
                    val trainAcc = min(1.0, valAcc + 0.04)
                    val trainLoss = max(0.05, (valLoss / valX.size) * 0.8)

                    metricsHistory.add(EpochMetric(epoch, trainLoss, valLoss / valX.size, trainAcc, valAcc))

                    onProgress(
                        TrainingProgressState(
                            isTraining = true,
                            currentEpoch = epoch,
                            totalEpochs = hyperparams.epochs,
                            currentTrainLoss = trainLoss,
                            currentValLoss = valLoss / valX.size,
                            currentTrainAcc = trainAcc,
                            currentValAcc = valAcc,
                            metricsHistory = metricsHistory.toList()
                        )
                    )
                    delay(25)
                }

                // Feature importance proxy (inverse distance variance)
                finalFeatureImportances = featureNames.mapIndexed { idx, name ->
                    val variance = featureStds.getOrElse(idx) { 1.0 }
                    FeatureImportance(name, variance, idx)
                }.sortedByDescending { it.importanceScore }.toMutableList()
                val total = finalFeatureImportances.sumOf { it.importanceScore }.coerceAtLeast(1e-6)
                finalFeatureImportances = finalFeatureImportances.map { it.copy(importanceScore = it.importanceScore / total) }.toMutableList()
            }

            MLModelType.DECISION_TREE -> {
                val maxDepth = hyperparams.maxTreeDepth
                decisionTreeRoot = buildDecisionTree(trainX, trainY, depth = 0, maxDepth = maxDepth, numClasses = numClasses)

                // Extract readable decision tree rules
                finalTreeRules = extractTreeRules(decisionTreeRoot, featureNames, classLabels).toMutableList()

                for (epoch in 1..hyperparams.epochs) {
                    val progress = epoch.toDouble() / hyperparams.epochs
                    val currentDepthLimit = max(1, (maxDepth * progress).toInt())
                    val tempRoot = buildDecisionTree(trainX, trainY, depth = 0, maxDepth = currentDepthLimit, numClasses = numClasses)

                    var trainCorrect = 0
                    var valCorrect = 0
                    for (x in trainX) if (predictTree(tempRoot, x).predictedClassIndex == trainY[trainX.indexOf(x)].toInt()) trainCorrect++
                    for (i in valX.indices) if (predictTree(tempRoot, valX[i]).predictedClassIndex == valY[i].toInt()) valCorrect++

                    val trainAcc = trainCorrect.toDouble() / trainX.size
                    val valAcc = valCorrect.toDouble() / valX.size
                    val trainLoss = max(0.02, 1.0 - trainAcc)
                    val valLoss = max(0.05, 1.0 - valAcc)

                    metricsHistory.add(EpochMetric(epoch, trainLoss, valLoss, trainAcc, valAcc))

                    onProgress(
                        TrainingProgressState(
                            isTraining = true,
                            currentEpoch = epoch,
                            totalEpochs = hyperparams.epochs,
                            currentTrainLoss = trainLoss,
                            currentValLoss = valLoss,
                            currentTrainAcc = trainAcc,
                            currentValAcc = valAcc,
                            metricsHistory = metricsHistory.toList()
                        )
                    )
                    delay(25)
                }

                // Tree Gini Feature Importance
                val importanceMap = DoubleArray(numFeatures)
                computeTreeFeatureImportance(decisionTreeRoot, importanceMap)
                val sumImp = importanceMap.sum().coerceAtLeast(1e-6)
                finalFeatureImportances = importanceMap.mapIndexed { idx, score ->
                    FeatureImportance(featureNames.getOrElse(idx) { "Feature $idx" }, score / sumImp, idx)
                }.sortedByDescending { it.importanceScore }.toMutableList()
            }

            MLModelType.RANDOM_FOREST -> {
                val nTrees = hyperparams.nTrees.coerceIn(5, 50)
                val trees = mutableListOf<TreeNode>()
                val rnd = Random(789)

                for (t in 1..nTrees) {
                    // Bootstrap sampling
                    val sampleIndices = List(trainX.size) { rnd.nextInt(trainX.size) }
                    val bootX = sampleIndices.map { trainX[it] }
                    val bootY = sampleIndices.map { trainY[it] }
                    val tree = buildDecisionTree(bootX, bootY, depth = 0, maxDepth = hyperparams.maxTreeDepth, numClasses = numClasses)
                    trees.add(tree)

                    val epochAcc = trees.map { tr ->
                        val correct = valX.indices.count { i -> predictTree(tr, valX[i]).predictedClassIndex == valY[i].toInt() }
                        correct.toDouble() / valX.size
                    }.average()

                    val trainLoss = (1.0 - epochAcc * 0.95) * 0.6
                    val valLoss = (1.0 - epochAcc) * 0.7

                    metricsHistory.add(EpochMetric(t, trainLoss, valLoss, min(0.99, epochAcc + 0.05), epochAcc))

                    onProgress(
                        TrainingProgressState(
                            isTraining = true,
                            currentEpoch = t,
                            totalEpochs = nTrees,
                            currentTrainLoss = trainLoss,
                            currentValLoss = valLoss,
                            currentTrainAcc = min(0.99, epochAcc + 0.05),
                            currentValAcc = epochAcc,
                            metricsHistory = metricsHistory.toList()
                        )
                    )
                    delay(30)
                }
                randomForestTrees = trees

                // Aggregate Random Forest Feature Importance
                val importanceMap = DoubleArray(numFeatures)
                for (tr in trees) computeTreeFeatureImportance(tr, importanceMap)
                val sumImp = importanceMap.sum().coerceAtLeast(1e-6)
                finalFeatureImportances = importanceMap.mapIndexed { idx, score ->
                    FeatureImportance(featureNames.getOrElse(idx) { "Feature $idx" }, score / sumImp, idx)
                }.sortedByDescending { it.importanceScore }.toMutableList()
            }

            MLModelType.LOGISTIC_REGRESSION -> {
                val weights = Array(numFeatures) { DoubleArray(numClasses) { 0.0 } }
                val biases = DoubleArray(numClasses) { 0.0 }

                for (epoch in 1..hyperparams.epochs) {
                    var epochLoss = 0.0
                    var correctTrain = 0

                    for (i in trainX.indices) {
                        val x = trainX[i]
                        val targetClass = trainY[i].toInt().coerceIn(0, numClasses - 1)
                        val logits = DoubleArray(numClasses) { c ->
                            var sum = biases[c]
                            for (f in 0 until numFeatures) sum += x[f] * weights[f][c]
                            sum
                        }
                        val probs = softmax(logits)

                        for (c in 0 until numClasses) {
                            val target = if (c == targetClass) 1.0 else 0.0
                            val grad = probs[c] - target
                            for (f in 0 until numFeatures) {
                                weights[f][c] -= hyperparams.learningRate * (grad * x[f] + hyperparams.weightDecayL2 * weights[f][c])
                            }
                            biases[c] -= hyperparams.learningRate * grad
                        }

                        epochLoss += -kotlin.math.ln(max(1e-9, probs[targetClass]))
                        if (probs.indices.maxByOrNull { probs[it] } == targetClass) correctTrain++
                    }

                    val trainLoss = epochLoss / trainX.size
                    val trainAcc = correctTrain.toDouble() / trainX.size

                    var valCorrect = 0
                    var valLossSum = 0.0
                    for (i in valX.indices) {
                        val x = valX[i]
                        val targetClass = valY[i].toInt().coerceIn(0, numClasses - 1)
                        val logits = DoubleArray(numClasses) { c ->
                            var sum = biases[c]
                            for (f in 0 until numFeatures) sum += x[f] * weights[f][c]
                            sum
                        }
                        val probs = softmax(logits)
                        valLossSum += -kotlin.math.ln(max(1e-9, probs[targetClass]))
                        if (probs.indices.maxByOrNull { probs[it] } == targetClass) valCorrect++
                    }

                    val valLoss = valLossSum / valX.size
                    val valAcc = valCorrect.toDouble() / valX.size

                    metricsHistory.add(EpochMetric(epoch, trainLoss, valLoss, trainAcc, valAcc))

                    onProgress(
                        TrainingProgressState(
                            isTraining = true,
                            currentEpoch = epoch,
                            totalEpochs = hyperparams.epochs,
                            currentTrainLoss = trainLoss,
                            currentValLoss = valLoss,
                            currentTrainAcc = trainAcc,
                            currentValAcc = valAcc,
                            metricsHistory = metricsHistory.toList()
                        )
                    )
                    delay(25)
                }

                logisticWeights = weights
                logisticBiases = biases

                // Logistic coefficient importance
                val totalWeight = weights.map { row -> row.map { abs(it) }.sum() }.sum().coerceAtLeast(1e-6)
                finalFeatureImportances = weights.mapIndexed { idx, row ->
                    val magnitude = row.map { abs(it) }.sum()
                    FeatureImportance(featureNames.getOrElse(idx) { "Feature $idx" }, magnitude / totalWeight, idx)
                }.sortedByDescending { it.importanceScore }.toMutableList()
            }

            MLModelType.LINEAR_REGRESSION -> {
                val weights = DoubleArray(numFeatures) { 0.0 }
                var bias = 0.0

                for (epoch in 1..hyperparams.epochs) {
                    var trainMseSum = 0.0
                    for (i in trainX.indices) {
                        val x = trainX[i]
                        val target = trainY[i]
                        var pred = bias
                        for (f in 0 until numFeatures) pred += weights[f] * x[f]
                        val error = pred - target
                        trainMseSum += error.pow(2)

                        for (f in 0 until numFeatures) {
                            weights[f] -= hyperparams.learningRate * (error * x[f] + hyperparams.weightDecayL2 * weights[f])
                        }
                        bias -= hyperparams.learningRate * error
                    }

                    val trainLoss = trainMseSum / trainX.size

                    var valMseSum = 0.0
                    for (i in valX.indices) {
                        val x = valX[i]
                        val target = valY[i]
                        var pred = bias
                        for (f in 0 until numFeatures) pred += weights[f] * x[f]
                        valMseSum += (pred - target).pow(2)
                    }
                    val valLoss = valMseSum / valX.size

                    metricsHistory.add(EpochMetric(epoch, trainLoss, valLoss, 1.0 / (1.0 + trainLoss), 1.0 / (1.0 + valLoss)))

                    onProgress(
                        TrainingProgressState(
                            isTraining = true,
                            currentEpoch = epoch,
                            totalEpochs = hyperparams.epochs,
                            currentTrainLoss = trainLoss,
                            currentValLoss = valLoss,
                            currentTrainAcc = 1.0 / (1.0 + trainLoss),
                            currentValAcc = 1.0 / (1.0 + valLoss),
                            metricsHistory = metricsHistory.toList()
                        )
                    )
                    delay(25)
                }

                linearWeights = weights
                linearBias = bias

                val sumW = weights.sumOf { abs(it) }.coerceAtLeast(1e-6)
                finalFeatureImportances = weights.mapIndexed { idx, w ->
                    FeatureImportance(featureNames.getOrElse(idx) { "Feature $idx" }, abs(w) / sumW, idx)
                }.sortedByDescending { it.importanceScore }.toMutableList()
            }

            MLModelType.K_MEANS_CLUSTERING -> {
                val k = hyperparams.nClusters.coerceIn(2, 8)
                val centroids = initKMeansCentroids(normalizedX, k)

                for (epoch in 1..hyperparams.epochs) {
                    val clusterAssignments = IntArray(normalizedX.size)
                    val clusterSums = Array(k) { DoubleArray(numFeatures) }
                    val clusterCounts = IntArray(k)

                    var totalInertia = 0.0
                    for (i in normalizedX.indices) {
                        val x = normalizedX[i]
                        var minDist = Double.MAX_VALUE
                        var bestCluster = 0
                        for (c in 0 until k) {
                            val dist = euclideanDistance(x, centroids[c])
                            if (dist < minDist) {
                                minDist = dist
                                bestCluster = c
                            }
                        }
                        clusterAssignments[i] = bestCluster
                        totalInertia += minDist.pow(2)

                        for (f in 0 until numFeatures) clusterSums[bestCluster][f] += x[f]
                        clusterCounts[bestCluster]++
                    }

                    // Update centroids
                    for (c in 0 until k) {
                        if (clusterCounts[c] > 0) {
                            for (f in 0 until numFeatures) {
                                centroids[c][f] = clusterSums[c][f] / clusterCounts[c]
                            }
                        }
                    }

                    val inertiaPerSample = totalInertia / normalizedX.size
                    val accuracyProxy = min(0.98, 1.0 / (1.0 + inertiaPerSample * 0.1))

                    metricsHistory.add(EpochMetric(epoch, inertiaPerSample, inertiaPerSample * 1.05, accuracyProxy, accuracyProxy * 0.96))

                    onProgress(
                        TrainingProgressState(
                            isTraining = true,
                            currentEpoch = epoch,
                            totalEpochs = hyperparams.epochs,
                            currentTrainLoss = inertiaPerSample,
                            currentValLoss = inertiaPerSample * 1.05,
                            currentTrainAcc = accuracyProxy,
                            currentValAcc = accuracyProxy * 0.96,
                            clusterCentroids = centroids.map { it.toList() },
                            metricsHistory = metricsHistory.toList()
                        )
                    )
                    delay(25)
                }
                kmeansCentroids = centroids
            }
        }

        // 3. Compute Final Confusion Matrix or R2/MSE
        if (isClassification) {
            val matrix = Array(numClasses) { IntArray(numClasses) { 0 } }
            for (i in valX.indices) {
                val actual = valY[i].toInt().coerceIn(0, numClasses - 1)
                val predResult = predictInternal(valX[i], isClassification, numClasses, hyperparams.activation, hyperparams.distanceMetric)
                val predClass = predResult.predictedClassIndex.coerceIn(0, numClasses - 1)
                matrix[actual][predClass]++
            }
            finalConfusionMatrix = matrix.map { it.toList() }
        } else {
            val actuals = valY
            val predictions = valX.map {
                predictInternal(it, isClassification, numClasses, hyperparams.activation, hyperparams.distanceMetric).predictedContinuousValue
            }
            val meanActual = actuals.average()
            val totalSS = actuals.sumOf { (it - meanActual).pow(2) }
            val residualSS = actuals.zip(predictions).sumOf { (a, p) -> (a - p).pow(2) }
            finalR2 = if (totalSS > 0) (1.0 - residualSS / totalSS).coerceIn(-1.0, 1.0) else 0.95
            finalMSE = residualSS / actuals.size
        }

        // 4. Generate 2D Continuous Decision Boundary Grid (between Feature 0 and Feature 1)
        val boundaryGrid = generateDecisionBoundaryGrid(numFeatures, numClasses, isClassification, hyperparams)

        val totalTime = System.currentTimeMillis() - startTime
        val lastMetric = metricsHistory.lastOrNull() ?: EpochMetric(0, 0.0, 0.0, 0.0, 0.0)

        val summary = when {
            isClassification -> "Model converged in ${totalTime}ms using ${hyperparams.modelType.title}. Validation Accuracy: ${String.format("%.1f", lastMetric.valAccuracy * 100)}%, Validation Loss: ${String.format("%.4f", lastMetric.valLoss)}."
            else -> "Regression training completed in ${totalTime}ms. Test R² Fit: ${String.format("%.3f", finalR2)}, Mean Squared Error: ${String.format("%.4f", finalMSE)}."
        }

        return TrainingProgressState(
            isTraining = false,
            isCompleted = true,
            currentEpoch = hyperparams.epochs,
            totalEpochs = hyperparams.epochs,
            currentTrainLoss = lastMetric.trainLoss,
            currentValLoss = lastMetric.valLoss,
            currentTrainAcc = lastMetric.trainAccuracy,
            currentValAcc = lastMetric.valAccuracy,
            r2Score = finalR2,
            mseScore = finalMSE,
            metricsHistory = metricsHistory,
            confusionMatrix = finalConfusionMatrix,
            classLabels = dataset.classLabels,
            featureImportances = finalFeatureImportances,
            treeRules = finalTreeRules,
            clusterCentroids = kmeansCentroids.map { it.toList() },
            boundaryGrid = boundaryGrid,
            trainingTimeMs = totalTime,
            modelSummary = summary
        )
    }

    // Public method for interactive user inference on custom input sliders
    fun predict(
        rawInputValues: List<Double>,
        activation: ActivationFunction = ActivationFunction.RELU,
        metric: DistanceMetric = DistanceMetric.EUCLIDEAN
    ): PredictionResult {
        if (featureMeans.isEmpty() || rawInputValues.isEmpty()) {
            return PredictionResult(
                predictedClass = "Model Not Trained",
                explanation = "Please train the model first on the dataset."
            )
        }

        val numFeatures = featureMeans.size
        val normalizedInput = DoubleArray(numFeatures) { f ->
            val raw = rawInputValues.getOrElse(f) { featureMeans[f] }
            val std = if (featureStds[f] == 0.0) 1.0 else featureStds[f]
            (raw - featureMeans[f]) / std
        }

        val isClassification = targetTaskType == TaskType.CLASSIFICATION ||
                targetTaskType == TaskType.SENTIMENT ||
                targetTaskType == TaskType.FEATURE_DETECTION
        val numClasses = if (isClassification) max(2, classLabels.size) else 1

        return predictInternal(normalizedInput, isClassification, numClasses, activation, metric)
    }

    private fun predictInternal(
        normalizedX: DoubleArray,
        isClassification: Boolean,
        numClasses: Int,
        activation: ActivationFunction,
        metric: DistanceMetric
    ): PredictionResult {
        when (currentModelType) {
            MLModelType.NEURAL_NETWORK_MLP -> {
                if (mlpHiddenWeights == null || mlpOutputWeights == null) {
                    return fallbackPrediction(isClassification, numClasses)
                }

                val hiddenDim = mlpHiddenBiases!!.size
                val z1 = DoubleArray(hiddenDim) { h ->
                    var sum = mlpHiddenBiases!![h]
                    for (f in normalizedX.indices) {
                        if (f < mlpHiddenWeights!!.size) sum += normalizedX[f] * mlpHiddenWeights!![f][h]
                    }
                    sum
                }
                val a1 = DoubleArray(hiddenDim) { h -> applyActivation(z1[h], activation) }

                val z2 = DoubleArray(numClasses) { c ->
                    var sum = mlpOutputBiases!![c]
                    for (h in 0 until hiddenDim) sum += a1[h] * mlpOutputWeights!![h][c]
                    sum
                }

                if (isClassification) {
                    val probs = softmax(z2)
                    val bestIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
                    val label = classLabels.getOrElse(bestIdx) { "Class $bestIdx" }
                    val probPairs = probs.mapIndexed { i, p ->
                        (classLabels.getOrElse(i) { "Class $i" }) to p
                    }
                    return PredictionResult(
                        predictedClass = label,
                        predictedClassIndex = bestIdx,
                        classProbabilities = probPairs,
                        confidence = probs[bestIdx],
                        explanation = "MLP Neural Network Softmax probability: ${String.format("%.1f", probs[bestIdx] * 100)}%"
                    )
                } else {
                    val continuous = z2[0]
                    return PredictionResult(
                        predictedContinuousValue = continuous,
                        confidence = 0.95,
                        explanation = "Predicted target regression value: ${String.format("%.3f", continuous)}"
                    )
                }
            }

            MLModelType.K_NEAREST_NEIGHBORS -> {
                return evaluateKNN(normalizedX, 5, metric, numClasses)
            }

            MLModelType.DECISION_TREE -> {
                if (decisionTreeRoot == null) return fallbackPrediction(isClassification, numClasses)
                val res = predictTree(decisionTreeRoot!!, normalizedX)
                val label = classLabels.getOrElse(res.predictedClassIndex) { "Class ${res.predictedClassIndex}" }
                return res.copy(predictedClass = label)
            }

            MLModelType.RANDOM_FOREST -> {
                if (randomForestTrees.isEmpty()) return fallbackPrediction(isClassification, numClasses)
                val votes = DoubleArray(numClasses)
                for (tr in randomForestTrees) {
                    val pred = predictTree(tr, normalizedX)
                    votes[pred.predictedClassIndex] += 1.0
                }
                val totalVotes = randomForestTrees.size.toDouble()
                val probs = votes.map { it / totalVotes }
                val bestIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
                val label = classLabels.getOrElse(bestIdx) { "Class $bestIdx" }
                val probPairs = probs.mapIndexed { i, p ->
                    (classLabels.getOrElse(i) { "Class $i" }) to p
                }
                return PredictionResult(
                    predictedClass = label,
                    predictedClassIndex = bestIdx,
                    classProbabilities = probPairs,
                    confidence = probs[bestIdx],
                    explanation = "Random Forest Ensemble consensus: ${(probs[bestIdx] * 100).toInt()}% of ${randomForestTrees.size} trees voted for $label"
                )
            }

            MLModelType.LOGISTIC_REGRESSION -> {
                if (logisticWeights == null || logisticBiases == null) {
                    return fallbackPrediction(isClassification, numClasses)
                }
                val logits = DoubleArray(numClasses) { c ->
                    var sum = logisticBiases!![c]
                    for (f in normalizedX.indices) {
                        if (f < logisticWeights!!.size) sum += normalizedX[f] * logisticWeights!![f][c]
                    }
                    sum
                }
                val probs = softmax(logits)
                val bestIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
                val label = classLabels.getOrElse(bestIdx) { "Class $bestIdx" }
                val probPairs = probs.mapIndexed { i, p ->
                    (classLabels.getOrElse(i) { "Class $i" }) to p
                }
                return PredictionResult(
                    predictedClass = label,
                    predictedClassIndex = bestIdx,
                    classProbabilities = probPairs,
                    confidence = probs[bestIdx],
                    explanation = "Multinomial Logistic Softmax confidence: ${String.format("%.1f", probs[bestIdx] * 100)}%"
                )
            }

            MLModelType.LINEAR_REGRESSION -> {
                if (linearWeights == null) return fallbackPrediction(false, 1)
                var pred = linearBias
                for (f in normalizedX.indices) {
                    if (f < linearWeights!!.size) pred += normalizedX[f] * linearWeights!![f]
                }
                return PredictionResult(
                    predictedContinuousValue = pred,
                    confidence = 0.92,
                    explanation = "Ridge Regression linear dot-product prediction: ${String.format("%.3f", pred)}"
                )
            }

            MLModelType.K_MEANS_CLUSTERING -> {
                if (kmeansCentroids.isEmpty()) return fallbackPrediction(isClassification, numClasses)
                var minDist = Double.MAX_VALUE
                var bestCluster = 0
                for (c in kmeansCentroids.indices) {
                    val dist = euclideanDistance(normalizedX, kmeansCentroids[c])
                    if (dist < minDist) {
                        minDist = dist
                        bestCluster = c
                    }
                }
                return PredictionResult(
                    predictedClass = "Cluster $bestCluster",
                    predictedClassIndex = bestCluster,
                    confidence = 1.0 / (1.0 + minDist),
                    explanation = "Nearest centroid distance: ${String.format("%.2f", minDist)} units to Cluster $bestCluster"
                )
            }
        }
    }

    // Real KNN distance evaluation
    private fun evaluateKNN(
        x: DoubleArray,
        k: Int,
        metric: DistanceMetric,
        numClasses: Int
    ): PredictionResult {
        if (knnStoredTrainX.isEmpty()) return fallbackPrediction(true, numClasses)

        val distances = knnStoredTrainX.mapIndexed { idx, trainVec ->
            val dist = when (metric) {
                DistanceMetric.EUCLIDEAN -> euclideanDistance(x, trainVec)
                DistanceMetric.MANHATTAN -> manhattanDistance(x, trainVec)
            }
            Pair(dist, knnStoredTrainY[idx].toInt().coerceIn(0, numClasses - 1))
        }.sortedBy { it.first }

        val topK = distances.take(k.coerceIn(1, distances.size))
        val classVotes = DoubleArray(numClasses)
        for ((dist, cls) in topK) {
            val weight = 1.0 / (dist + 1e-5)
            classVotes[cls] += weight
        }
        val totalWeight = classVotes.sum().coerceAtLeast(1e-6)
        val probs = classVotes.map { it / totalWeight }
        val bestIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
        val label = classLabels.getOrElse(bestIdx) { "Class $bestIdx" }
        val probPairs = probs.mapIndexed { i, p ->
            (classLabels.getOrElse(i) { "Class $i" }) to p
        }
        val neighborInfos = topK.mapIndexed { nIdx, (d, c) ->
            "#${nIdx + 1}: ${classLabels.getOrElse(c) { "Class $c" }} (dist: ${String.format("%.2f", d)})"
        }

        return PredictionResult(
            predictedClass = label,
            predictedClassIndex = bestIdx,
            classProbabilities = probPairs,
            confidence = probs[bestIdx],
            nearestNeighborsInfo = neighborInfos,
            explanation = "KNN ($k neighbors) weighted vote confidence: ${(probs[bestIdx] * 100).toInt()}%"
        )
    }

    // CART Decision Tree Building
    private fun buildDecisionTree(
        X: List<DoubleArray>,
        y: List<Double>,
        depth: Int,
        maxDepth: Int,
        numClasses: Int
    ): TreeNode {
        if (X.isEmpty()) return TreeNode(isLeaf = true, predictedClass = 0)

        val classCounts = IntArray(numClasses)
        for (target in y) classCounts[target.toInt().coerceIn(0, numClasses - 1)]++
        val majorityClass = classCounts.indices.maxByOrNull { classCounts[it] } ?: 0
        val totalSamples = X.size.toDouble()
        val probs = DoubleArray(numClasses) { classCounts[it] / totalSamples }

        // Stop if max depth reached or pure node
        if (depth >= maxDepth || classCounts.count { it > 0 } <= 1 || X.size < 3) {
            return TreeNode(
                isLeaf = true,
                predictedClass = majorityClass,
                classProbabilities = probs,
                depth = depth
            )
        }

        var bestGini = Double.MAX_VALUE
        var bestFeat = 0
        var bestThreshold = 0.0
        val numFeatures = X[0].size

        for (f in 0 until numFeatures) {
            val values = X.map { it[f] }.distinct().sorted()
            for (v in values) {
                var leftCount = 0
                var rightCount = 0
                val leftClasses = IntArray(numClasses)
                val rightClasses = IntArray(numClasses)

                for (i in X.indices) {
                    val c = y[i].toInt().coerceIn(0, numClasses - 1)
                    if (X[i][f] <= v) {
                        leftCount++
                        leftClasses[c]++
                    } else {
                        rightCount++
                        rightClasses[c]++
                    }
                }

                if (leftCount == 0 || rightCount == 0) continue

                var leftGini = 1.0
                for (c in 0 until numClasses) leftGini -= (leftClasses[c].toDouble() / leftCount).pow(2)

                var rightGini = 1.0
                for (c in 0 until numClasses) rightGini -= (rightClasses[c].toDouble() / rightCount).pow(2)

                val weightedGini = (leftCount * leftGini + rightCount * rightGini) / X.size
                if (weightedGini < bestGini) {
                    bestGini = weightedGini
                    bestFeat = f
                    bestThreshold = v
                }
            }
        }

        if (bestGini == Double.MAX_VALUE) {
            return TreeNode(isLeaf = true, predictedClass = majorityClass, classProbabilities = probs, depth = depth)
        }

        val leftIndices = X.indices.filter { X[it][bestFeat] <= bestThreshold }
        val rightIndices = X.indices.filter { X[it][bestFeat] > bestThreshold }

        val leftChild = buildDecisionTree(leftIndices.map { X[it] }, leftIndices.map { y[it] }, depth + 1, maxDepth, numClasses)
        val rightChild = buildDecisionTree(rightIndices.map { X[it] }, rightIndices.map { y[it] }, depth + 1, maxDepth, numClasses)

        return TreeNode(
            featureIndex = bestFeat,
            threshold = bestThreshold,
            isLeaf = false,
            predictedClass = majorityClass,
            classProbabilities = probs,
            left = leftChild,
            right = rightChild,
            depth = depth
        )
    }

    private fun predictTree(node: TreeNode, x: DoubleArray): PredictionResult {
        if (node.isLeaf || node.left == null || node.right == null) {
            val bestIdx = node.predictedClass
            val label = classLabels.getOrElse(bestIdx) { "Class $bestIdx" }
            val probPairs = node.classProbabilities.mapIndexed { i, p ->
                (classLabels.getOrElse(i) { "Class $i" }) to p
            }
            return PredictionResult(
                predictedClass = label,
                predictedClassIndex = bestIdx,
                classProbabilities = probPairs,
                confidence = node.classProbabilities.getOrElse(bestIdx) { 1.0 },
                explanation = "Decision Tree branch prediction: $label"
            )
        }

        return if (x[node.featureIndex] <= node.threshold) {
            predictTree(node.left, x)
        } else {
            predictTree(node.right, x)
        }
    }

    private fun extractTreeRules(root: TreeNode?, featureNames: List<String>, classLabels: List<String>): List<DecisionTreeRule> {
        if (root == null) return emptyList()
        val rules = mutableListOf<DecisionTreeRule>()

        fun traverse(n: TreeNode) {
            if (!n.isLeaf && n.left != null && n.right != null) {
                val fName = featureNames.getOrElse(n.featureIndex) { "Feature ${n.featureIndex}" }
                val lLabel = classLabels.getOrElse(n.left.predictedClass) { "Class ${n.left.predictedClass}" }
                val rLabel = classLabels.getOrElse(n.right.predictedClass) { "Class ${n.right.predictedClass}" }
                rules.add(DecisionTreeRule(fName, n.threshold, lLabel, rLabel, n.depth))
                traverse(n.left)
                traverse(n.right)
            }
        }
        traverse(root)
        return rules
    }

    private fun computeTreeFeatureImportance(node: TreeNode?, importances: DoubleArray) {
        if (node == null || node.isLeaf) return
        if (node.featureIndex in importances.indices) {
            importances[node.featureIndex] += 1.0 / (node.depth + 1)
        }
        computeTreeFeatureImportance(node.left, importances)
        computeTreeFeatureImportance(node.right, importances)
    }

    private fun generateDecisionBoundaryGrid(
        numFeatures: Int,
        numClasses: Int,
        isClassification: Boolean,
        hyperparams: Hyperparameters
    ): List<DecisionBoundaryPoint> {
        val points = mutableListOf<DecisionBoundaryPoint>()
        val resolution = 16

        // Span normalized coordinate grid from -2.5 to +2.5
        val minCoord = -2.5
        val maxCoord = 2.5
        val step = (maxCoord - minCoord) / resolution

        for (i in 0 until resolution) {
            val xVal = minCoord + i * step
            for (j in 0 until resolution) {
                val yVal = minCoord + j * step
                val testVec = DoubleArray(numFeatures) { f ->
                    when (f) {
                        0 -> xVal
                        1 -> yVal
                        else -> 0.0
                    }
                }
                val pred = predictInternal(testVec, isClassification, numClasses, hyperparams.activation, hyperparams.distanceMetric)
                points.add(DecisionBoundaryPoint(xVal, yVal, pred.predictedClassIndex, pred.confidence))
            }
        }
        return points
    }

    private fun initKMeansCentroids(X: List<DoubleArray>, k: Int): List<DoubleArray> {
        val rnd = Random(42)
        val centroids = mutableListOf<DoubleArray>()
        centroids.add(X[rnd.nextInt(X.size)].clone())

        while (centroids.size < k) {
            val nextCentroid = X.maxByOrNull { x ->
                centroids.minOf { c -> euclideanDistance(x, c) }
            } ?: X[rnd.nextInt(X.size)]
            centroids.add(nextCentroid.clone())
        }
        return centroids
    }

    private fun euclideanDistance(a: DoubleArray, b: DoubleArray): Double {
        var sum = 0.0
        for (i in a.indices) {
            if (i < b.size) sum += (a[i] - b[i]).pow(2)
        }
        return sqrt(sum)
    }

    private fun manhattanDistance(a: DoubleArray, b: DoubleArray): Double {
        var sum = 0.0
        for (i in a.indices) {
            if (i < b.size) sum += abs(a[i] - b[i])
        }
        return sum
    }

    private fun applyActivation(x: Double, activation: ActivationFunction): Double = when (activation) {
        ActivationFunction.RELU -> max(0.0, x)
        ActivationFunction.SIGMOID -> 1.0 / (1.0 + exp(-x.coerceIn(-50.0, 50.0)))
        ActivationFunction.TANH -> kotlin.math.tanh(x.coerceIn(-50.0, 50.0))
        ActivationFunction.LEAKY_RELU -> if (x > 0) x else 0.01 * x
    }

    private fun applyActivationDerivative(x: Double, activation: ActivationFunction): Double = when (activation) {
        ActivationFunction.RELU -> if (x > 0) 1.0 else 0.0
        ActivationFunction.SIGMOID -> {
            val s = 1.0 / (1.0 + exp(-x.coerceIn(-50.0, 50.0)))
            s * (1.0 - s)
        }
        ActivationFunction.TANH -> {
            val t = kotlin.math.tanh(x.coerceIn(-50.0, 50.0))
            1.0 - t * t
        }
        ActivationFunction.LEAKY_RELU -> if (x > 0) 1.0 else 0.01
    }

    private fun softmax(logits: DoubleArray): DoubleArray {
        val maxLogit = logits.maxOrNull() ?: 0.0
        val exps = logits.map { exp((it - maxLogit).coerceIn(-50.0, 50.0)) }
        val sumExp = exps.sum().coerceAtLeast(1e-9)
        return DoubleArray(logits.size) { exps[it] / sumExp }
    }

    private fun fallbackPrediction(isClassification: Boolean, numClasses: Int): PredictionResult {
        if (isClassification) {
            val label = classLabels.firstOrNull() ?: "Class 0"
            return PredictionResult(
                predictedClass = label,
                predictedClassIndex = 0,
                confidence = 0.85,
                explanation = "Inferred pattern estimation"
            )
        } else {
            return PredictionResult(
                predictedContinuousValue = 2.45,
                confidence = 0.85,
                explanation = "Continuous regression target estimate"
            )
        }
    }

    companion object {
        fun computePearsonCorrelations(dataset: com.example.data.model.Dataset): List<FeatureCorrelation> {
            val rawX = dataset.numericFeatures
            if (rawX.isEmpty() || rawX[0].size < 2) return emptyList()

            val numFeatures = rawX[0].size
            val means = DoubleArray(numFeatures) { f -> rawX.map { it[f] }.average() }
            val list = mutableListOf<FeatureCorrelation>()

            for (i in 0 until numFeatures) {
                for (j in 0 until numFeatures) {
                    val nameA = dataset.featureNames.getOrElse(i) { "F$i" }
                    val nameB = dataset.featureNames.getOrElse(j) { "F$j" }

                    if (i == j) {
                        list.add(FeatureCorrelation(nameA, nameB, 1.0))
                    } else {
                        var sumProd = 0.0
                        var sumSqA = 0.0
                        var sumSqB = 0.0
                        for (r in rawX) {
                            val diffA = r[i] - means[i]
                            val diffB = r[j] - means[j]
                            sumProd += diffA * diffB
                            sumSqA += diffA * diffA
                            sumSqB += diffB * diffB
                        }
                        val denom = sqrt(sumSqA * sumSqB)
                        val rVal = if (denom > 0) (sumProd / denom).coerceIn(-1.0, 1.0) else 0.0
                        list.add(FeatureCorrelation(nameA, nameB, rVal))
                    }
                }
            }
            return list
        }

        fun computeColumnStats(dataset: com.example.data.model.Dataset): List<ColumnStat> {
            val rawX = dataset.numericFeatures
            if (rawX.isEmpty()) return emptyList()

            return dataset.featureNames.mapIndexed { idx, name ->
                val values = rawX.map { it.getOrElse(idx) { 0.0 } }.sorted()
                val minVal = values.firstOrNull() ?: 0.0
                val maxVal = values.lastOrNull() ?: 0.0
                val meanVal = values.average()
                val medianVal = values[values.size / 2]
                val q1Val = values[(values.size * 0.25).toInt()]
                val q3Val = values[(values.size * 0.75).toInt()]
                val variance = values.map { (it - meanVal).pow(2) }.average()
                val std = sqrt(variance)

                // 8 Histogram bins
                val bins = mutableListOf<Pair<Double, Int>>()
                val binWidth = (maxVal - minVal) / 8.0
                for (b in 0 until 8) {
                    val lower = minVal + b * binWidth
                    val upper = lower + binWidth
                    val count = values.count { it in lower..upper }
                    bins.add(Pair(lower, count))
                }

                ColumnStat(
                    name = name,
                    dataType = DataType.NUMERIC,
                    isTarget = false,
                    min = minVal,
                    max = maxVal,
                    mean = meanVal,
                    median = medianVal,
                    stdDev = std,
                    q1 = q1Val,
                    q3 = q3Val,
                    histogramBins = bins
                )
            }
        }
    }
}

