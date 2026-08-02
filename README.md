# 📱 KMM Econometric Engine (Kotlin Multiplatform Mobile & Desktop)

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-blue.svg)](https://kotlinlang.org/)
[![KMM](https://img.shields.io/badge/KMM-Multiplatform-purple.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![JVM](https://img.shields.io/badge/JVM-17%2B-red.svg)](https://www.oracle.com/java/)
[![Tests](https://img.shields.io/badge/Unit_Tests-12_Passed-brightgreen.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A generic, production-ready **Kotlin Multiplatform Mobile (KMM) & Desktop Econometric App** featuring **8 Integrated Data Sources** (Data.gov Census/BLS, FRED St. Louis Fed, World Bank, Yahoo Finance, EIA Energy, Scraped Panel Data, Drag & Drop CSV/JSON/TSV), dynamic schema auto-detection, aligned console reporting, 300 DPI chart exports, and an automated **JUnit 5 Unit & Integration Test Suite**.

---

## ⚡ Root Build, Test, & Execution Commands

Run these single-line commands directly from the project root (`/Users/brentzey/personal/econometric_archetype_kmp`):

### **1. Run Full Unit & Integration Test Suite**
```bash
gradle test
```

### **2. Build All Targets & Executable Fat JAR**
```bash
gradle build
```

### **3. Run Desktop / CLI Application**
```bash
# Option A: Executable Fat JAR (Production Mode)
java -jar build/libs/econometric_archetype_kmp-1.0.0.jar

# Option B: Gradle Runner (Dev Mode)
gradle run
```

### **4. Ultimate One-Liner (Test + Build + Run)**
```bash
gradle test && gradle build && java -jar build/libs/econometric_archetype_kmp-1.0.0.jar
```

---

## 🌐 8 Integrated Multiplatform Data Sources (`DataIngestionService.kt`)

1. **🏛️ FRED (St. Louis Fed Federal Reserve Data)**: Real GDP, CPI, Unemployment Rate, Effective Fed Funds Rate, M2 Money Supply, S&P 500.
2. **🏛️ Data.gov U.S. Census Bureau**: Advance Retail Sales, Housing Starts, Total Construction Spending.
3. **📊 Data.gov BLS (Bureau of Labor Statistics)**: Nonfarm Payroll Employment, Average Hourly Earnings, Producer Price Index.
4. **🌍 World Bank Open Data**: Global Country Panel ($N=50$) covering GDP Per Capita, Inflation Rate, Trade Openness (% of GDP).
5. **📈 Yahoo Finance Market Data**: S&P 500 Index (`^GSPC`), WTI Crude Oil Futures (`CL=F`), Gold Futures (`GC=F`), EUR/USD FX Rate (`EURUSD=X`).
6. **⚡ U.S. EIA Energy Information Administration**: U.S. Crude Oil Stocks, Retail Gasoline Prices.
7. **🛒 Product Tracker Scraped Panel Data**: $N=10$ products across $T=100$ periods ($N \times T = 1,000$ obs) with supply instruments.
8. **📂 Multiplatform Drag & Drop Parser (`DragAndDropHandler.kt`)**: Drag & drop `.csv`, `.tsv`, `.json`, `.ndjson`, or `.parquet` files directly into Desktop/CLI or Mobile file pickers with auto-schema classification (**Panel**, **Time-Series Macro**, **Cross-Sectional**).

---

## 🧪 Comprehensive Unit & Integration Test Suite (`commonTest` & `jvmTest`)

All 12 test cases pass cleanly:

```text
DataIngestionTest > testYahooFinanceIngestion() PASSED
DataIngestionTest > testDataGovBlsIngestion() PASSED
DataIngestionTest > testEiaEnergyIngestion() PASSED
DataIngestionTest > testWorldBankIngestion() PASSED
DataIngestionTest > testDataGovCensusIngestion() PASSED
DataIngestionTest > testFredDataIngestion() PASSED
DataIngestionTest > testDragAndDropCsvParser() PASSED
EconometricEngineTest > testSyntheticPanelDataGeneration() PASSED
JvmRegressionRunnerTest > testFredRegressionEstimation() PASSED
JvmRegressionRunnerTest > testTwoStageLeastSquaresIvEstimation() PASSED
JvmRegressionRunnerTest > testPooledOlsEstimation() PASSED
JvmRegressionRunnerTest > testFixedEffectsEstimation() PASSED
```

---

## 📁 Repository Structure

```
econometric_archetype_kmp/
├── build.gradle.kts
├── settings.gradle.kts
├── src/
│   ├── commonMain/kotlin/com/econometrics/
│   │   ├── model/DataMatrix.kt               (TabularDataset, DataColumn, DatasetSource)
│   │   ├── engine/GenericEconometricEngine.kt (Panel Data Generator, Estimator Interfaces)
│   │   ├── ingestion/DataIngestionService.kt (FRED, Data.gov Census/BLS, World Bank, Yahoo, EIA)
│   │   ├── ingestion/DragAndDropHandler.kt   (Multiplatform Drop Target Listener)
│   │   └── viewmodel/EconometricViewModel.kt  (Shared Coroutine StateFlow UI ViewModel)
│   ├── commonTest/kotlin/com/econometrics/
│   │   ├── DataIngestionTest.kt              (Unit Tests for 8 Data Sources)
│   │   └── EconometricEngineTest.kt          (Unit Tests for Panel Generator)
│   ├── jvmMain/kotlin/com/econometrics/
│   │   ├── Main.kt                           (8 Data Ingestion Demos, 1:1 Aligned Tables)
│   │   └── engine/JvmRegressionRunner.kt    (Apache Commons Math 3 Matrix Solvers)
│   └── jvmTest/kotlin/com/econometrics/
│       └── JvmRegressionRunnerTest.kt        (Integration Tests for OLS, FE, 2SLS IV)
└── output_reports/
    ├── kmm_scraped_demand_curve.png
    └── kmm_fred_gdp_timeseries.png
```

---

## 🔗 Companion Repositories

* 🐍 **Python Econometric Suite**: [product_tracker_app](https://github.com/brentmzey/product_tracker_app)
* ☕ **Kotlin Econometric Suite**: [product_tracker_kotlin](https://github.com/brentmzey/product_tracker_kotlin)
# econometric_archetype_kmp
