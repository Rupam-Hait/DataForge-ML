package com.example.ai

import com.example.data.model.ColumnStat
import com.example.data.model.DataType
import com.example.data.model.Dataset
import com.example.data.model.DatasetCategory
import com.example.data.model.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

object GeminiDatasetService {

    suspend fun generateSyntheticDataset(
        topicPrompt: String,
        category: DatasetCategory = DatasetCategory.TABULAR,
        taskType: TaskType = TaskType.CLASSIFICATION
    ): Dataset = withContext(Dispatchers.IO) {
        val apiKey = try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val generated = callGeminiApi(apiKey, topicPrompt, category, taskType)
                if (generated != null) return@withContext generated
            } catch (e: Exception) {
                // Fallback to internal generator on network/quota exception
            }
        }

        // Offline / Fallback synthetic dataset generator
        generateOfflineSyntheticDataset(topicPrompt, category, taskType)
    }

    suspend fun getModelAdvisorRecommendation(dataset: Dataset): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = "Act as an expert ML engineer. For this dataset: '${dataset.title}' (${dataset.taskType.displayName}, ${dataset.featuresCount} features, ${dataset.samplesCount} samples, target='${dataset.targetColumn}'). Suggest: 1. Recommended Model Architecture 2. Best Learning Rate & Batch Size 3. Data Preprocessing Tips 4. Loss Function. Keep response under 120 words with clear bullet points."
                val response = callGeminiRawText(apiKey, prompt)
                if (response.isNotBlank()) return@withContext response
            } catch (e: Exception) {
                // Fallback
            }
        }

        // Fallback advisory logic
        when (dataset.taskType) {
            TaskType.CLASSIFICATION, TaskType.SENTIMENT, TaskType.FEATURE_DETECTION -> """
• Recommended Model: Multi-Layer Perceptron (MLP) with 2 hidden layers (32 -> 16 units) and ReLU activation.
• Optimizer & LR: Adam Optimizer with lr=0.01 and weight decay 1e-4.
• Loss Function: Categorical Cross-Entropy (Softmax).
• Preprocessing: Apply Z-score standardization on numerical features; one-hot encode categorical columns.
• Regularization: Use Dropout (0.2) to prevent overfitting on small validation splits.
            """.trimIndent()

            TaskType.REGRESSION -> """
• Recommended Model: Ridge / ElasticNet Regression for baseline, followed by a 3-layer MLP Neural Network.
• Optimizer & LR: Adam with initial learning rate 0.05, batch size 16.
• Loss Function: Mean Squared Error (MSE) with L2 weight regularization.
• Preprocessing: Normalize feature bounds using StandardScaler; check for skewness with log1p transformation.
• Evaluation: Monitor R² score alongside Mean Absolute Error (MAE).
            """.trimIndent()

            TaskType.CLUSTERING -> """
• Recommended Model: K-Means with k-means++ centroid initialization or DBSCAN for density estimation.
• Metrics: Silhouette Score and Davies-Bouldin Index.
• Preprocessing: Crucial to perform Min-Max normalization [0, 1] so distance metrics aren't dominated by high-magnitude features.
            """.trimIndent()
        }
    }

    private fun callGeminiApi(
        apiKey: String,
        prompt: String,
        category: DatasetCategory,
        taskType: TaskType
    ): Dataset? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 25000

        val systemPrompt = """
Generate a machine learning training dataset in JSON format about: "$prompt".
Return ONLY raw JSON with this exact structure:
{
  "title": "Dataset Title",
  "description": "Short explanation",
  "targetColumn": "target_name",
  "featureNames": ["feat1", "feat2", "feat3", "feat4"],
  "classLabels": ["ClassA", "ClassB"],
  "rows": [
    {"feat1": 1.2, "feat2": 3.4, "feat3": 0.5, "feat4": 2.1, "target_name": "ClassA"},
    ... (at least 15 realistic rows)
  ]
}
""".trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

        if (conn.responseCode == 200) {
            val responseText = conn.inputStream.bufferedReader().readText()
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates") ?: return null
            val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val rawText = parts.optJSONObject(0)?.optString("text") ?: return null

            val cleanJson = rawText.substringAfter("```json").substringAfter("```").substringBeforeLast("```").trim()
            val dataObj = JSONObject(cleanJson)

            val title = dataObj.optString("title", "AI Synthesized Dataset: $prompt")
            val desc = dataObj.optString("description", "Synthetic data generated for machine training on $prompt.")
            val targetCol = dataObj.optString("targetColumn", "target")
            val featNamesJson = dataObj.optJSONArray("featureNames") ?: JSONArray()
            val featNames = (0 until featNamesJson.length()).map { featNamesJson.getString(it) }
            val classLabelsJson = dataObj.optJSONArray("classLabels") ?: JSONArray()
            val classLabels = (0 until classLabelsJson.length()).map { classLabelsJson.getString(it) }

            val rowsJson = dataObj.optJSONArray("rows") ?: JSONArray()
            val sampleRows = mutableListOf<Map<String, String>>()
            val numericFeatures = mutableListOf<List<Double>>()
            val numericTargets = mutableListOf<Double>()

            for (i in 0 until rowsJson.length()) {
                val row = rowsJson.getJSONObject(i)
                val map = mutableMapOf<String, String>()
                val featureVector = mutableListOf<Double>()

                for (fn in featNames) {
                    val num = row.optDouble(fn, 0.0)
                    featureVector.add(num)
                    map[fn] = String.format("%.2f", num)
                }

                val targetVal = row.optString(targetCol, "")
                map[targetCol] = targetVal
                sampleRows.add(map)
                numericFeatures.add(featureVector)

                val targetIdx = classLabels.indexOf(targetVal)
                if (targetIdx >= 0) {
                    numericTargets.add(targetIdx.toDouble())
                } else {
                    numericTargets.add(row.optDouble(targetCol, 0.0))
                }
            }

            return Dataset(
                id = "ds_ai_" + System.currentTimeMillis(),
                title = title,
                description = desc,
                category = category,
                taskType = taskType,
                samplesCount = sampleRows.size,
                featuresCount = featNames.size,
                targetColumn = targetCol,
                tags = listOf("AI Synthesized", "Gemini 3.5 Flash", category.displayName),
                classLabels = classLabels,
                featureNames = featNames,
                sampleRows = sampleRows,
                numericFeatures = numericFeatures,
                numericTargets = numericTargets,
                isCustom = true,
                rating = 4.9,
                downloadsCount = 1
            )
        }
        return null
    }

    private fun callGeminiRawText(apiKey: String, prompt: String): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 15000

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
        if (conn.responseCode == 200) {
            val responseText = conn.inputStream.bufferedReader().readText()
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates") ?: return ""
            val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            return parts.optJSONObject(0)?.optString("text", "") ?: ""
        }
        return ""
    }

    private fun generateOfflineSyntheticDataset(
        topicPrompt: String,
        category: DatasetCategory,
        taskType: TaskType
    ): Dataset {
        val rnd = Random(System.currentTimeMillis())
        val cleanTopic = topicPrompt.trim().ifBlank { "Custom Neural Pattern" }
        val isClassification = taskType != TaskType.REGRESSION

        val featNames = listOf("feature_alpha", "feature_beta", "feature_gamma", "feature_delta")
        val classLabels = if (isClassification) listOf("Normal / Type A", "Anomalous / Type B") else emptyList()
        val targetCol = if (isClassification) "classification_label" else "target_score"

        val numericFeatures = mutableListOf<List<Double>>()
        val numericTargets = mutableListOf<Double>()
        val sampleRows = mutableListOf<Map<String, String>>()

        val count = 20
        for (i in 0 until count) {
            val isClass1 = i >= count / 2
            val f1 = (if (isClass1) 6.5 else 2.5) + rnd.nextDouble() * 2.0
            val f2 = (if (isClass1) 12.0 else 4.0) + rnd.nextDouble() * 3.0
            val f3 = (if (isClass1) 0.85 else 0.25) + rnd.nextDouble() * 0.15
            val f4 = (if (isClass1) 45.0 else 15.0) + rnd.nextDouble() * 10.0

            numericFeatures.add(listOf(f1, f2, f3, f4))

            if (isClassification) {
                val target = if (isClass1) 1.0 else 0.0
                numericTargets.add(target)
                sampleRows.add(
                    mapOf(
                        "feature_alpha" to String.format("%.2f", f1),
                        "feature_beta" to String.format("%.2f", f2),
                        "feature_gamma" to String.format("%.2f", f3),
                        "feature_delta" to String.format("%.1f", f4),
                        targetCol to classLabels[target.toInt()]
                    )
                )
            } else {
                val continuous = f1 * 1.5 + f2 * 0.4 + rnd.nextDouble() * 2.0
                numericTargets.add(continuous)
                sampleRows.add(
                    mapOf(
                        "feature_alpha" to String.format("%.2f", f1),
                        "feature_beta" to String.format("%.2f", f2),
                        "feature_gamma" to String.format("%.2f", f3),
                        "feature_delta" to String.format("%.1f", f4),
                        targetCol to String.format("%.2f", continuous)
                    )
                )
            }
        }

        return Dataset(
            id = "ds_synthetic_" + System.currentTimeMillis(),
            title = "$cleanTopic Dataset",
            description = "Synthetic data generated for training machine learning models on $cleanTopic.",
            category = category,
            taskType = taskType,
            samplesCount = count,
            featuresCount = 4,
            targetColumn = targetCol,
            tags = listOf("Synthetic Data", "Custom Forge", category.displayName),
            classLabels = classLabels,
            featureNames = featNames,
            sampleRows = sampleRows,
            numericFeatures = numericFeatures,
            numericTargets = numericTargets,
            isCustom = true,
            rating = 4.85,
            downloadsCount = 1
        )
    }
}
