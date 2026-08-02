package com.econometrics.model

enum class DatasetSource {
    FRED_ST_LOUIS_FED,
    DATA_GOV_CENSUS,
    DATA_GOV_BEA,
    DATA_GOV_BLS,
    WORLD_BANK_OPEN_DATA,
    YAHOO_FINANCE_MARKET_DATA,
    EIA_ENERGY_ADMINISTRATION,
    PRODUCT_TRACKER_SCRAPED,
    USER_DRAG_AND_DROP_CSV,
    USER_DRAG_AND_DROP_JSON,
    USER_DRAG_AND_DROP_PARQUET
}

enum class DatasetType {
    CROSS_SECTIONAL,
    PANEL_DATA,
    TIME_SERIES_MACRO
}

enum class ModelEstimator {
    POOLED_OLS_HC3,
    FIXED_EFFECTS_FE,
    RANDOM_EFFECTS_RE,
    TWO_STAGE_LEAST_SQUARES_IV,
    LINEAR_PROBABILITY_LPM,
    LOGIT_AME,
    PROBIT_AME,
    VECTOR_AUTOREGRESSION_VAR
}

data class ColumnMetadata(
    val name: String,
    val unitOfMeasure: String,
    val isDependent: Boolean = false,
    val isEndogenous: Boolean = false,
    val isInstrument: Boolean = false,
    val sourceId: String? = null
)

data class DataColumn(
    val metadata: ColumnMetadata,
    val values: DoubleArray
) {
    val mean: Double get() = if (values.isEmpty()) 0.0 else values.average()
    val stdDev: Double get() {
        if (values.size < 2) return 0.0
        val m = mean
        val sumSq = values.sumOf { (it - m) * (it - m) }
        return kotlin.math.sqrt(sumSq / (values.size - 1))
    }
    val min: Double get() = values.minOrNull() ?: 0.0
    val max: Double get() = values.maxOrNull() ?: 0.0
    val median: Double get() {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0 else sorted[mid]
    }
}

data class TabularDataset(
    val title: String,
    val source: DatasetSource,
    val datasetType: DatasetType,
    val entityColumn: String? = null,
    val timeColumn: String? = null,
    val columns: Map<String, DataColumn>,
    val timeStamps: List<String> = emptyList()
) {
    val sampleSize: Int get() = columns.values.firstOrNull()?.values?.size ?: 0
}

data class RegressionOutput(
    val estimator: ModelEstimator,
    val estimatorName: String,
    val intercept: Double,
    val coefficients: Map<String, Double>,
    val standardErrors: Map<String, Double>,
    val tStatistics: Map<String, Double>,
    val pValues: Map<String, Double>,
    val rSquared: Double,
    val adjustedRSquared: Double,
    val diagnosticSummary: String
)
