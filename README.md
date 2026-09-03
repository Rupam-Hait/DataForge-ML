# ⚡ DataForge ML — On-Device Machine Learning Studio & Dataset Synthesizer

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/AI-Gemini_3.7_Flash-FF6F00?style=for-the-badge&logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" />
</p>

**DataForge ML** is a full-fledged on-device Machine Learning Studio, Data Engineering Pipeline, and Dataset Hub built with Kotlin and Jetpack Compose. It empowers engineers, data scientists, and students to curate data, perform statistical Exploratory Data Analysis (EDA), preprocess & scale features, train machine learning algorithms locally with live loss/accuracy and 2D continuous decision boundary visualizers, test real-time interactive predictions, and export production-ready code for **PyTorch 2.x**, **TensorFlow / Keras**, **Scikit-Learn**, **FastAPI Server**, and **ONNX Runtime**.

---

## 📑 Table of Contents
- [📱 How to Open & Run the App](#-how-to-open--run-the-app)
- [🌟 Key Architecture & Capabilities](#-key-architecture--capabilities)
- [🧠 Machine Learning Algorithms](#-machine-learning-algorithms)
- [🛠️ Feature Engineering & Preprocessing](#️-feature-engineering--preprocessing)
- [📦 Multi-Framework Code Exporters](#-multi-framework-code-exporters)
- [🏗️ Project Structure](#️-project-structure)
- [🚀 Quickstart & Build Commands](#-quickstart--build-commands)
- [📄 License](#-license)

---

## 📱 How to Open & Run the App

DataForge ML is a native Android application built with modern Jetpack Compose. You can run and interact with it using any of the methods below:

### Option 1: Inside Antigravity / IDE (Live Emulator Panel)
- If you are running inside the **Antigravity IDE**, look at the **Device Emulator / Interactive Preview panel** on the right side or top bar of your workspace window.
- The compiled app runs live in the interactive Android emulator container.

### Option 2: In Android Studio (Recommended for Development)
1. Launch **Android Studio** (Ladybug, Meerkat, or newer).
2. Click **Open** and select the project folder:
   ```
   C:\Users\haitr\antigravity\DataForge-ML
   ```
3. Wait for Gradle sync to complete.
4. Select your emulator (AVD) or connected physical Android device from the device dropdown.
5. Click the green **Run ▶** button (or press `Shift + F10`) to build, install, and launch the app.

### Option 3: Build & Install via Command Line
To build and install the debug APK directly to an attached Android phone or emulator:

1. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The generated APK will be at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Install to Device via ADB**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Launch the App**:
   ```bash
   adb shell am start -n com.aistudio.dataforge.kx8m2p/com.example.MainActivity
   ```

---

## 🌟 Key Architecture & Capabilities

```
                       ┌─────────────────────────────────────────────────────────┐
                       │                   DataForge ML Studio                   │
                       └────────────────────────────┬────────────────────────────┘
                                                    │
        ┌───────────────────┬───────────────────────┼───────────────────────┬───────────────────┐
        │                   │                       │                       │                   │
┌───────▼───────┐   ┌───────▼───────┐       ┌───────▼───────┐       ┌───────▼───────┐   ┌───────▼───────┐
│  Dataset Hub  │   │   EDA & EDA   │       │ Preprocessing │       │  On-Device ML │   │ Multi-Deploy  │
│  & CSV Ingest │   │ Profiling Hub │       │    Studio     │       │    Engine     │   │   Exporters   │
└───────┬───────┘   └───────┬───────┘       └───────┬───────┘       └───────┬───────┘   └───────┬───────┘
        │                   │                       │                       │                   │
  • Multi-modal       • Pearson Matrix        • Standard Z-Score      • Adam MLP          • PyTorch 2.x
  • Gemini Synth      • Distributions         • Min-Max Scaler        • Decision Tree     • TF / TFLite
  • Custom Room       • Summary Stats         • Robust IQR Scaler     • Random Forest     • Scikit-Learn
  • CSV Ingestor      • Feature Vectors       • Polynomials (x²)      • 2D Boundary Grid  • FastAPI Server
                                              • Outlier Filters       • KNN & K-Means     • ONNX Runtime
```

### 1. 🌐 Dataset Hub & CSV Pipeline Ingestion
- **Multi-Modal Dataset Hub**: Preloaded with production datasets spanning **Tabular**, **Computer Vision Embeddings**, **Natural Language Processing (NLP)**, **Time Series**, and **Sensor Telemetry**.
- **CSV & JSON Ingestion Engine**: Automatically parses custom CSV text, auto-detects column data types, identifies targets, and extracts statistical parameters.
- **Offline Room Database**: Full local persistence for user-created and AI-synthesized datasets.

### 2. 🤖 Gemini 3.7 Flash AI Synthesizer & Intelligent Advisor
- **Synthetic Dataset Synthesizer**: Generate multi-dimensional synthetic machine learning datasets for specialized domains (*e.g., Drone Battery Degradation, Smart Agriculture Soil Sensors, Cyber Defense Network Flows, DeFi Liquidity Pools*) with automated schema and target distributions.
- **Architectural Advisor**: Analyzes dataset structure, sample-to-feature ratios, and class balance to deliver tailored architectural recommendations (*learning rates, optimizers, regularization weights*).

### 3. 📊 Exploratory Data Analysis (EDA) & Profiling
- **Pearson Correlation Matrix Heatmap**: Computes and renders $N \times N$ correlation coefficients ($r \in [-1.0, 1.0]$) with dynamic color gradient intensities to detect multicollinearity.
- **Feature Distribution & Density Inspector**: Interactive histogram visualizer with statistical metrics (*Mean, Median, Standard Deviation, Q1, Q3, Min, Max*).
- **Feature Schema & Vector Inspector**: Inspect raw and normalized high-dimensional numerical vectors.

### 4. 🎨 2D Continuous Decision Boundary Visualizer
- Visualizes the 2D spatial classification partition surface across normalized feature domains.
- Background mesh color-coded by model confidence gradients, overlaid with class-colored dataset points and halos.

### 5. 🎮 Interactive Prediction Sandbox
- Real-time parameter sliders allowing users to adjust feature inputs and execute instant inference against trained model weights.
- Displays predicted class labels, continuous regression outputs, confidence bars, and top nearest neighbor distance readouts.

---

## 🧠 Machine Learning Algorithms

| Algorithm | Type | Supported Tasks | Key Optimizers & Parameters |
| :--- | :--- | :--- | :--- |
| **Multi-Layer Perceptron (MLP)** | Deep Neural Net | Classification, Regression, Sentiment | **Adam**, RMSProp, Momentum, SGD, L2 Decay, Dropout |
| **Random Forest Ensemble** | Bagging Ensemble | Classification, Feature Selection | Bootstrap Aggregation, Gini Feature Importance, $N$ Trees |
| **Decision Tree (CART)** | Tree Classifier | Classification, Feature Selection | Recursive Binary Gini Impurity Split, Rule Extraction |
| **K-Nearest Neighbors (KNN)** | Instance-Based | Classification, Sentiment | Euclidean (L2) & Manhattan (L1) Distance, Weighted Voting |
| **Multinomial Logistic Regression** | Linear Softmax | Classification, Sentiment | L1/L2 Regularized Softmax Gradient Descent |
| **Ridge Linear Regression** | Linear Continuous | Regression | Mean Squared Error (MSE), L2 Tikhonov Regularization |
| **K-Means Clustering** | Unsupervised | Clustering, Anomaly Detection | Lloyd's Algorithm, K-Means++ Initialization |

---

## 🛠️ Feature Engineering & Preprocessing

- **Scalers**:
  - **Standard Z-Score Standardization**: $x' = \frac{x - \mu}{\sigma}$
  - **Min-Max Normalization**: $x' = \frac{x - \min(x)}{\max(x) - \min(x)}$
  - **Robust Scaler**: $x' = \frac{x - \text{median}}{\text{IQR}}$
  - **L2 Unit Normalizer**: $x' = \frac{x}{\|x\|_2}$
- **Outlier Strategies**: Trim 1.5x IQR outliers or filter 3-Sigma z-score anomalies.
- **Synthetic Feature Expansion**: Generate Polynomial Features ($x_1^2, x_1 x_2$) and cross-feature interaction ratios.
- **Train / Validation Stratified Splitter**: Configurable test split ratios.

---

## 📦 Multi-Framework Code Exporters

- **PyTorch 2.x**: Copyable script with `nn.Module`, DataLoader, Adam optimizer, train loop, and **TorchScript JIT** export.
- **TensorFlow / Keras 3**: Complete pipeline with `tf.keras.Sequential`, EarlyStopping, and **TensorFlow Lite (TFLite)** quantization.
- **Scikit-Learn**: Automated `Pipeline`, `StandardScaler`, Classifier/Regressor, and **Joblib** serialization.
- **FastAPI Microservice Server**: Production REST API script with Pydantic validation and `/predict` endpoint.
- **ONNX Runtime Engine**: Python script executing high-performance inference with `onnxruntime`.
- **CSV & JSON**: Instant export of raw records and schemas.

---

## 🏗️ Project Structure

```
DataForge-ML/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt               # Navigation host & tab controller
│   │   │   │   ├── ai/
│   │   │   │   │   └── GeminiDatasetService.kt   # Gemini 3.7 Flash dataset generation & advisor
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/                    # Room DB (AppDatabase, Dao, Entities)
│   │   │   │   │   ├── model/                    # Dataset, MLModels, Hyperparameters, Preprocessing
│   │   │   │   │   └── repository/               # DatasetRepository, PreloadedDatasets
│   │   │   │   ├── ml/
│   │   │   │   │   ├── engine/
│   │   │   │   │   │   └── MLEngine.kt           # On-device ML engine (MLP, Trees, KNN, K-Means)
│   │   │   │   │   └── export/
│   │   │   │   │       └── CodeExportGenerator.kt# PyTorch, TensorFlow, FastAPI, ONNX, Scikit-Learn
│   │   │   │   └── ui/
│   │   │   │       ├── components/               # DecisionBoundaryVisualizer, Heatmaps, Charts
│   │   │   │       ├── dialogs/                  # AI Generator, Manual & CSV Import dialogs
│   │   │   │       ├── screens/                  # Hub, Detail, Preprocess, Trainer, Export, History
│   │   │   │       ├── theme/                    # Cyberpunk dark scientific theme
│   │   │   │       └── viewmodel/                # MainViewModel
```

---

## 🚀 Quickstart & Build Commands

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- Android SDK 36 (minSdk 24)
- JDK 17 / 21

### Building and Running
```bash
# Clone the repository
git clone https://github.com/Rupam-Hait/DataForge-ML.git
cd DataForge-ML

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

---

## 📄 License
This project is open-source and licensed under the [MIT License](LICENSE).