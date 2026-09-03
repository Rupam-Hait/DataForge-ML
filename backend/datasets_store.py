# Preloaded Datasets Store for DataForge ML Backend
import numpy as np

PRELOADED_DATASETS = [
    {
        "id": "ds_iris_flower",
        "title": "Iris Flower Botanical Dataset",
        "description": "Standard multi-class benchmark for botanical pattern classification based on sepal and petal morphological measurements.",
        "category": "TABULAR",
        "taskType": "CLASSIFICATION",
        "samplesCount": 60,
        "featuresCount": 4,
        "targetColumn": "species",
        "difficulty": "Beginner",
        "tags": ["Morphology", "Multi-Class", "Benchmark", "Botany"],
        "classLabels": ["Setosa", "Versicolor", "Virginica"],
        "featureNames": ["Sepal Length (cm)", "Sepal Width (cm)", "Petal Length (cm)", "Petal Width (cm)"],
        "numericFeatures": [
            [5.1, 3.5, 1.4, 0.2], [4.9, 3.0, 1.4, 0.2], [4.7, 3.2, 1.3, 0.2], [4.6, 3.1, 1.5, 0.2], [5.0, 3.6, 1.4, 0.2],
            [5.4, 3.9, 1.7, 0.4], [4.6, 3.4, 1.4, 0.3], [5.0, 3.4, 1.5, 0.2], [4.4, 2.9, 1.4, 0.2], [4.9, 3.1, 1.5, 0.1],
            [5.4, 3.7, 1.5, 0.2], [4.8, 3.4, 1.6, 0.2], [4.8, 3.0, 1.4, 0.1], [4.3, 3.0, 1.1, 0.1], [5.8, 4.0, 1.2, 0.2],
            [5.7, 4.4, 1.5, 0.4], [5.4, 3.9, 1.3, 0.4], [5.1, 3.5, 1.4, 0.3], [5.7, 3.8, 1.7, 0.3], [5.1, 3.8, 1.5, 0.3],
            [7.0, 3.2, 4.7, 1.4], [6.4, 3.2, 4.5, 1.5], [6.9, 3.1, 4.9, 1.5], [5.5, 2.3, 4.0, 1.3], [6.5, 2.8, 4.6, 1.5],
            [5.7, 2.8, 4.5, 1.3], [6.3, 3.3, 4.7, 1.6], [4.9, 2.4, 3.3, 1.0], [6.6, 2.9, 4.6, 1.3], [5.2, 2.7, 3.9, 1.4],
            [5.0, 2.0, 3.5, 1.0], [5.9, 3.0, 4.2, 1.5], [6.0, 2.2, 4.0, 1.0], [6.1, 2.9, 4.7, 1.4], [5.6, 2.9, 3.6, 1.3],
            [6.7, 3.1, 4.4, 1.4], [5.6, 3.0, 4.5, 1.5], [5.8, 2.7, 4.1, 1.0], [6.2, 2.2, 4.5, 1.5], [5.6, 2.5, 3.9, 1.1],
            [6.3, 3.3, 6.0, 2.5], [5.8, 2.7, 5.1, 1.9], [7.1, 3.0, 5.9, 2.1], [6.3, 2.9, 5.6, 1.8], [6.5, 3.0, 5.8, 2.2],
            [7.6, 3.0, 6.6, 2.1], [4.9, 2.5, 4.5, 1.7], [7.3, 2.9, 6.3, 1.8], [6.7, 2.5, 5.8, 1.8], [7.2, 3.6, 6.1, 2.5],
            [6.5, 3.2, 5.1, 2.0], [6.4, 2.7, 5.3, 1.9], [6.8, 3.0, 5.5, 2.1], [5.7, 2.5, 5.0, 2.0], [5.8, 2.8, 5.1, 2.4],
            [6.4, 3.2, 5.3, 2.3], [6.5, 3.0, 5.5, 1.8], [7.7, 3.8, 6.7, 2.2], [7.7, 2.6, 6.9, 2.3], [6.0, 2.2, 5.0, 1.5]
        ],
        "numericTargets": [0.0]*20 + [1.0]*20 + [2.0]*20,
        "sampleRows": [
            {"sepal_length": "5.1", "sepal_width": "3.5", "petal_length": "1.4", "petal_width": "0.2", "species": "Setosa"},
            {"sepal_length": "7.0", "sepal_width": "3.2", "petal_length": "4.7", "petal_width": "1.4", "species": "Versicolor"},
            {"sepal_length": "6.3", "sepal_width": "3.3", "petal_length": "6.0", "petal_width": "2.5", "species": "Virginica"}
        ]
    },
    {
        "id": "ds_heart_disease",
        "title": "Cardiovascular Clinical Diagnostic Dataset",
        "description": "Biomedical patient parameters for training binary medical screening classifiers and diagnostic decision trees.",
        "category": "TABULAR",
        "taskType": "CLASSIFICATION",
        "samplesCount": 30,
        "featuresCount": 6,
        "targetColumn": "diagnosis",
        "difficulty": "Intermediate",
        "tags": ["Healthcare", "Cardiology", "Diagnostics", "Binary"],
        "classLabels": ["Low Risk (Healthy)", "High Risk (Cardiac)"],
        "featureNames": ["Age (years)", "Resting Blood Pressure", "Serum Cholesterol", "Max Heart Rate", "ST Depression", "Exercise Angina"],
        "numericFeatures": [
            [63.0, 145.0, 233.0, 150.0, 2.3, 1.0], [37.0, 130.0, 250.0, 187.0, 3.5, 0.0], [41.0, 130.0, 204.0, 172.0, 1.4, 0.0],
            [56.0, 120.0, 236.0, 178.0, 0.8, 0.0], [57.0, 120.0, 354.0, 163.0, 0.6, 1.0], [57.0, 140.0, 192.0, 148.0, 0.4, 0.0],
            [56.0, 140.0, 294.0, 153.0, 1.3, 0.0], [44.0, 120.0, 263.0, 173.0, 0.0, 0.0], [52.0, 172.0, 199.0, 162.0, 0.5, 0.0],
            [57.0, 150.0, 168.0, 174.0, 1.6, 0.0], [54.0, 140.0, 239.0, 160.0, 1.2, 0.0], [48.0, 130.0, 275.0, 139.0, 0.2, 0.0],
            [49.0, 130.0, 266.0, 171.0, 0.6, 0.0], [64.0, 110.0, 211.0, 144.0, 1.8, 1.0], [58.0, 150.0, 283.0, 162.0, 1.0, 0.0],
            [67.0, 160.0, 286.0, 108.0, 1.5, 1.0], [67.0, 120.0, 229.0, 129.0, 2.6, 1.0], [62.0, 130.0, 231.0, 103.0, 1.4, 1.0],
            [53.0, 140.0, 203.0, 155.0, 3.1, 1.0], [58.0, 114.0, 318.0, 140.0, 4.4, 0.0], [58.0, 170.0, 225.0, 146.0, 2.8, 1.0],
            [46.0, 140.0, 311.0, 120.0, 1.8, 1.0], [53.0, 142.0, 226.0, 111.0, 0.0, 1.0], [65.0, 135.0, 254.0, 127.0, 2.8, 1.0],
            [48.0, 130.0, 256.0, 150.0, 0.0, 1.0], [63.0, 130.0, 330.0, 132.0, 1.8, 1.0], [65.0, 110.0, 248.0, 158.0, 0.6, 0.0],
            [60.0, 140.0, 293.0, 170.0, 1.2, 1.0], [59.0, 140.0, 177.0, 162.0, 0.0, 1.0], [57.0, 140.0, 241.0, 123.0, 0.2, 1.0]
        ],
        "numericTargets": [0.0]*15 + [1.0]*15,
        "sampleRows": [
            {"age": "63", "resting_bp": "145", "cholesterol": "233", "max_hr": "150", "diagnosis": "Low Risk (Healthy)"},
            {"age": "67", "resting_bp": "160", "cholesterol": "286", "max_hr": "108", "diagnosis": "High Risk (Cardiac)"}
        ]
    },
    {
        "id": "ds_california_housing",
        "title": "California Real Estate Housing Price",
        "description": "Continuous regression dataset predicting residential values from geographic & economic metrics.",
        "category": "TABULAR",
        "taskType": "REGRESSION",
        "samplesCount": 20,
        "featuresCount": 5,
        "targetColumn": "median_house_value",
        "difficulty": "Intermediate",
        "tags": ["Regression", "Real Estate", "Economics"],
        "classLabels": [],
        "featureNames": ["Median Income ($10k)", "House Age", "Avg Rooms", "Population", "Avg Occupants"],
        "numericFeatures": [
            [8.32, 41.0, 6.98, 322.0, 2.55], [8.30, 21.0, 6.23, 2401.0, 2.10], [7.25, 52.0, 8.28, 496.0, 2.80],
            [5.64, 52.0, 5.81, 558.0, 2.54], [3.84, 52.0, 6.28, 565.0, 2.18], [4.03, 52.0, 4.76, 413.0, 2.13],
            [3.65, 52.0, 4.93, 1094.0, 2.12], [3.12, 52.0, 4.79, 1157.0, 1.78], [2.08, 42.0, 4.29, 1206.0, 2.02],
            [3.69, 52.0, 4.97, 1551.0, 2.17], [3.20, 52.0, 5.47, 910.0, 2.29], [3.27, 52.0, 4.77, 1504.0, 2.13],
            [3.07, 52.0, 5.32, 1098.0, 2.30], [2.67, 52.0, 4.04, 345.0, 1.98], [1.91, 52.0, 4.26, 1212.0, 1.94],
            [2.12, 50.0, 4.24, 697.0, 2.64], [2.77, 52.0, 5.44, 990.0, 2.92], [2.12, 52.0, 4.05, 648.0, 1.95],
            [1.99, 50.0, 5.34, 990.0, 2.36], [2.60, 52.0, 5.46, 690.0, 2.61]
        ],
        "numericTargets": [4.52, 3.58, 3.52, 3.41, 3.42, 2.69, 2.99, 2.41, 2.26, 2.61, 2.81, 2.41, 2.13, 1.91, 1.59, 1.40, 1.52, 1.55, 1.58, 1.62],
        "sampleRows": [
            {"median_income": "8.32", "house_age": "41", "avg_rooms": "6.98", "median_house_value": "$452.0k"},
            {"median_income": "3.84", "house_age": "52", "avg_rooms": "6.28", "median_house_value": "$342.0k"}
        ]
    }
]

def calculate_correlations_and_stats(dataset):
    X = np.array(dataset["numericFeatures"], dtype=float)
    feature_names = dataset["featureNames"]
    
    correlations = []
    if X.shape[0] > 1 and X.shape[1] > 1:
        corr_matrix = np.corrcoef(X, rowvar=False)
        for i in range(len(feature_names)):
            for j in range(i + 1, len(feature_names)):
                val = float(corr_matrix[i, j])
                if np.isnan(val): val = 0.0
                correlations.append({
                    "featureA": feature_names[i],
                    "featureB": feature_names[j],
                    "coefficient": round(val, 3)
                })
                
    column_stats = []
    for i, name in enumerate(feature_names):
        col = X[:, i]
        column_stats.append({
            "name": name,
            "min": round(float(np.min(col)), 2),
            "max": round(float(np.max(col)), 2),
            "mean": round(float(np.mean(col)), 2),
            "stdDev": round(float(np.std(col)), 2),
            "q1": round(float(np.percentile(col, 25)), 2),
            "q3": round(float(np.percentile(col, 75)), 2)
        })
        
    ds_copy = dict(dataset)
    ds_copy["correlations"] = correlations
    ds_copy["columnStats"] = column_stats
    return ds_copy
