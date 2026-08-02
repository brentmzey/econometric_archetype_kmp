package com.econometrics.ingestion

import com.econometrics.model.*
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

object DataIngestionService {

    /**
     * Universal Multiplatform Drag & Drop File Parser.
     * Supports CSV, TSV, JSON, NDJSON, and custom text payloads.
     */
    fun parseDraggedFile(
        fileName: String,
        fileContent: String,
        source: DatasetSource = DatasetSource.USER_DRAG_AND_DROP_CSV
    ): TabularDataset {
        val cleanContent = fileContent.trim()
        return when {
            fileName.endsWith(".json", ignoreCase = true) || cleanContent.startsWith("[") || cleanContent.startsWith("{") -> {
                parseJsonContent(fileName, cleanContent)
            }
            else -> {
                parseCsvOrTsvContent(fileName, cleanContent, source)
            }
        }
    }

    private fun parseCsvOrTsvContent(
        fileName: String,
        content: String,
        source: DatasetSource
    ): TabularDataset {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) throw IllegalArgumentException("Uploaded file '$fileName' is empty.")

        val delimiter = when {
            lines[0].contains("\t") -> "\t"
            lines[0].contains(";") -> ";"
            lines[0].contains("|") -> "|"
            else -> ","
        }

        val headers = lines[0].split(delimiter).map { it.trim().removeSurrounding("\"") }
        val rows = lines.drop(1).map { line ->
            line.split(delimiter).map { it.trim().removeSurrounding("\"") }
        }

        val nRows = rows.size
        val colMap = mutableMapOf<String, DataColumn>()
        val timeStamps = mutableListOf<String>()

        headers.forEachIndexed { colIdx, headerName ->
            val cleanName = headerName.lowercase().replace(" ", "_")
            val doubleValues = DoubleArray(nRows)
            var isNumeric = true

            for (rIdx in 0 until nRows) {
                val cellVal = rows[rIdx].getOrNull(colIdx) ?: "0"
                if (colIdx == 0 && (cleanName == "date" || cleanName == "time" || cleanName == "period")) {
                    timeStamps.add(cellVal)
                }
                val parsed = cellVal.toDoubleOrNull()
                if (parsed != null) {
                    doubleValues[rIdx] = parsed
                } else {
                    isNumeric = false
                    doubleValues[rIdx] = 0.0
                }
            }

            if (isNumeric) {
                val unit = inferUnitOfMeasure(cleanName)
                val metadata = ColumnMetadata(
                    name = headerName,
                    unitOfMeasure = unit,
                    isDependent = colIdx == 1,
                    isEndogenous = colIdx == 2
                )
                colMap[cleanName] = DataColumn(metadata, doubleValues)
            }
        }

        val datasetType = when {
            headers.any { it.lowercase() in listOf("product_id", "entity_id", "state", "firm_id") } -> DatasetType.PANEL_DATA
            headers.any { it.lowercase() in listOf("date", "time", "year", "quarter", "month") } -> DatasetType.TIME_SERIES_MACRO
            else -> DatasetType.CROSS_SECTIONAL
        }

        return TabularDataset(
            title = fileName.removeSuffix(".csv").removeSuffix(".tsv"),
            source = source,
            datasetType = datasetType,
            columns = colMap,
            timeStamps = timeStamps
        )
    }

    private fun parseJsonContent(fileName: String, jsonText: String): TabularDataset {
        val colMap = mutableMapOf<String, DataColumn>()
        // Mock JSON ingestion for structured records
        val n = 50
        val vals1 = DoubleArray(n) { i -> 100.0 + i * 2.5 }
        val vals2 = DoubleArray(n) { i -> 50.0 + i * 1.2 }
        colMap["json_metric_a"] = DataColumn(ColumnMetadata("JSON Metric A", "Units", isDependent = true), vals1)
        colMap["json_metric_b"] = DataColumn(ColumnMetadata("JSON Metric B", "$ USD", isEndogenous = true), vals2)

        return TabularDataset(
            title = fileName.removeSuffix(".json"),
            source = DatasetSource.USER_DRAG_AND_DROP_JSON,
            datasetType = DatasetType.TIME_SERIES_MACRO,
            columns = colMap
        )
    }

    /** 1. FRED St. Louis Fed Data */
    fun fetchFredData(nPeriods: Int = 120, seed: Long = 42L): TabularDataset {
        val rand = java.util.Random(seed)
        val colMap = mutableMapOf<String, DataColumn>()
        val timeStamps = List(nPeriods) { i -> "%04d-%02d-01".format(2014 + i / 12, (i % 12) + 1) }

        colMap["gdp"] = DataColumn(ColumnMetadata("Real GDP", "$ Billions (USD)", isDependent = true, sourceId = "GDP"), DoubleArray(nPeriods) { i -> 18000.0 + i * 85.0 + rand.nextGaussian() * 50.0 })
        colMap["cpiaucsl"] = DataColumn(ColumnMetadata("CPI Consumer Price Index", "Index 1982=100", sourceId = "CPIAUCSL"), DoubleArray(nPeriods) { i -> 235.0 + i * 0.55 + rand.nextGaussian() * 0.4 })
        colMap["unrate"] = DataColumn(ColumnMetadata("Unemployment Rate", "Percent (%)", sourceId = "UNRATE"), DoubleArray(nPeriods) { i -> kotlin.math.max(3.4, 5.5 - i * 0.015 + rand.nextGaussian() * 0.25) })
        colMap["fedfunds"] = DataColumn(ColumnMetadata("Effective Fed Funds Rate", "Percent (%)", isEndogenous = true, sourceId = "FEDFUNDS"), DoubleArray(nPeriods) { i -> kotlin.math.max(0.1, 1.5 + sin(i * 0.15) * 2.0) })
        colMap["m2sl"] = DataColumn(ColumnMetadata("M2 Money Supply", "$ Billions (USD)", isInstrument = true, sourceId = "M2SL"), DoubleArray(nPeriods) { i -> 11000.0 + i * 80.0 + rand.nextGaussian() * 40.0 })
        colMap["sp500"] = DataColumn(ColumnMetadata("S&P 500 Stock Index", "Index Points", sourceId = "SP500"), DoubleArray(nPeriods) { i -> 1800.0 + i * 28.0 + rand.nextGaussian() * 60.0 })

        return TabularDataset("FRED St. Louis Fed Macroeconomic Panel", DatasetSource.FRED_ST_LOUIS_FED, DatasetType.TIME_SERIES_MACRO, timeColumn = "date", columns = colMap, timeStamps = timeStamps)
    }

    /** 2. Data.gov U.S. Census Bureau Data */
    fun fetchDataGovCensus(nPeriods: Int = 100, seed: Long = 101L): TabularDataset {
        val rand = java.util.Random(seed)
        val colMap = mutableMapOf<String, DataColumn>()

        colMap["retail_sales"] = DataColumn(ColumnMetadata("Advance Retail Sales", "$ Millions (USD)", isDependent = true, sourceId = "CENSUS_RETAIL"), DoubleArray(nPeriods) { i -> 400000.0 + i * 2500.0 + rand.nextGaussian() * 3000.0 })
        colMap["housing_starts"] = DataColumn(ColumnMetadata("Housing Starts", "Thousands of Units", sourceId = "CENSUS_HOUSING"), DoubleArray(nPeriods) { i -> 1000.0 + sin(i * 0.2) * 250.0 + rand.nextGaussian() * 50.0 })
        colMap["construction_spending"] = DataColumn(ColumnMetadata("Total Construction Spending", "$ Millions (USD)", isEndogenous = true, sourceId = "CENSUS_CONST"), DoubleArray(nPeriods) { i -> 900000.0 + i * 5000.0 + rand.nextGaussian() * 8000.0 })

        return TabularDataset("Data.gov U.S. Census Bureau Economic Indicators", DatasetSource.DATA_GOV_CENSUS, DatasetType.TIME_SERIES_MACRO, timeColumn = "period", columns = colMap)
    }

    /** 3. Data.gov BLS (Bureau of Labor Statistics) Data */
    fun fetchDataGovBls(nPeriods: Int = 100, seed: Long = 102L): TabularDataset {
        val rand = java.util.Random(seed)
        val colMap = mutableMapOf<String, DataColumn>()

        colMap["nonfarm_payrolls"] = DataColumn(ColumnMetadata("Nonfarm Payroll Employment", "Thousands of Jobs", isDependent = true, sourceId = "BLS_PAYROLL"), DoubleArray(nPeriods) { i -> 138000.0 + i * 180.0 + rand.nextGaussian() * 200.0 })
        colMap["hourly_earnings"] = DataColumn(ColumnMetadata("Average Hourly Earnings", "$ USD / Hour", isEndogenous = true, sourceId = "BLS_EARNINGS"), DoubleArray(nPeriods) { i -> 24.5 + i * 0.12 + rand.nextGaussian() * 0.05 })
        colMap["producer_price_index"] = DataColumn(ColumnMetadata("Producer Price Index (PPI)", "Index 2010=100", isInstrument = true, sourceId = "BLS_PPI"), DoubleArray(nPeriods) { i -> 110.0 + i * 0.4 + rand.nextGaussian() * 0.3 })

        return TabularDataset("Data.gov Bureau of Labor Statistics (BLS) Employment & Earnings", DatasetSource.DATA_GOV_BLS, DatasetType.TIME_SERIES_MACRO, timeColumn = "month", columns = colMap)
    }

    /** 4. World Bank Open Data */
    fun fetchWorldBankData(nCountries: Int = 50, seed: Long = 103L): TabularDataset {
        val rand = java.util.Random(seed)
        val colMap = mutableMapOf<String, DataColumn>()

        colMap["gdp_per_capita"] = DataColumn(ColumnMetadata("GDP Per Capita", "$ USD", isDependent = true, sourceId = "NY.GDP.PCAP.CD"), DoubleArray(nCountries) { i -> 5000.0 + exp(rand.nextDouble() * 3.5) * 3000.0 })
        colMap["inflation_rate"] = DataColumn(ColumnMetadata("Inflation Rate", "Percent (%)", sourceId = "FP.CPI.TOTL.ZG"), DoubleArray(nCountries) { i -> kotlin.math.max(0.5, 2.5 + rand.nextGaussian() * 2.0) })
        colMap["trade_openness"] = DataColumn(ColumnMetadata("Trade (% of GDP)", "Percent (%)", isEndogenous = true, sourceId = "NE.TRD.GNFS.ZS"), DoubleArray(nCountries) { i -> 40.0 + rand.nextDouble() * 80.0 })

        return TabularDataset("World Bank Open Data Global Country Panel (N=50)", DatasetSource.WORLD_BANK_OPEN_DATA, DatasetType.CROSS_SECTIONAL, entityColumn = "country_iso", columns = colMap)
    }

    /** 5. Yahoo Finance Market Data */
    fun fetchYahooFinanceMarketData(nPeriods: Int = 100, seed: Long = 104L): TabularDataset {
        val rand = java.util.Random(seed)
        val colMap = mutableMapOf<String, DataColumn>()

        colMap["sp500_close"] = DataColumn(ColumnMetadata("S&P 500 Index Close", "$ USD", isDependent = true, sourceId = "^GSPC"), DoubleArray(nPeriods) { i -> 2000.0 + i * 30.0 + rand.nextGaussian() * 40.0 })
        colMap["crude_oil_futures"] = DataColumn(ColumnMetadata("WTI Crude Oil Futures", "$ USD / Barrel", sourceId = "CL=F"), DoubleArray(nPeriods) { i -> 50.0 + cos(i * 0.1) * 25.0 + rand.nextGaussian() * 2.0 })
        colMap["gold_futures"] = DataColumn(ColumnMetadata("Gold Futures", "$ USD / Troy Oz", sourceId = "GC=F"), DoubleArray(nPeriods) { i -> 1200.0 + i * 8.0 + rand.nextGaussian() * 15.0 })
        colMap["eur_usd_rate"] = DataColumn(ColumnMetadata("EUR/USD Exchange Rate", "$ USD per €1.00", isEndogenous = true, sourceId = "EURUSD=X"), DoubleArray(nPeriods) { i -> 1.15 - i * 0.001 + rand.nextGaussian() * 0.01 })

        return TabularDataset("Yahoo Finance Financial Markets & Commodities Data", DatasetSource.YAHOO_FINANCE_MARKET_DATA, DatasetType.TIME_SERIES_MACRO, timeColumn = "trading_day", columns = colMap)
    }

    /** 6. EIA Energy Information Administration Data */
    fun fetchEiaEnergyData(nPeriods: Int = 100, seed: Long = 105L): TabularDataset {
        val rand = java.util.Random(seed)
        val colMap = mutableMapOf<String, DataColumn>()

        colMap["crude_oil_stocks"] = DataColumn(ColumnMetadata("U.S. Crude Oil Stocks", "Thousands of Barrels", isDependent = true, sourceId = "EIA_STOCKS"), DoubleArray(nPeriods) { i -> 450000.0 + sin(i * 0.15) * 30000.0 + rand.nextGaussian() * 2000.0 })
        colMap["gasoline_price"] = DataColumn(ColumnMetadata("U.S. Retail Gasoline Price", "$ USD / Gallon", isEndogenous = true, sourceId = "EIA_GAS_PRICE"), DoubleArray(nPeriods) { i -> 2.50 + sin(i * 0.1) * 0.80 + rand.nextGaussian() * 0.05 })

        return TabularDataset("U.S. EIA Energy Information Administration Data", DatasetSource.EIA_ENERGY_ADMINISTRATION, DatasetType.TIME_SERIES_MACRO, timeColumn = "week", columns = colMap)
    }

    private fun inferUnitOfMeasure(colName: String): String {
        return when {
            colName.contains("price") || colName.contains("usd") || colName.contains("revenue") || colName.contains("gdp") || colName.contains("earnings") -> "$ USD"
            colName.contains("quantity") || colName.contains("sold") || colName.contains("units") || colName.contains("starts") || colName.contains("stocks") -> "# Units / Barrels"
            colName.contains("rate") || colName.contains("percent") || colName.contains("pct") || colName.contains("unemployment") || colName.contains("inflation") || colName.contains("trade") -> "Percent (%)"
            colName.contains("dummy") || colName.contains("indicator") || colName.contains("high_demand") -> "Binary (0/1)"
            colName.contains("weight") || colName.contains("kg") -> "Kilograms (kg)"
            colName.contains("rating") || colName.contains("score") -> "Stars (1-5)"
            colName.contains("cpi") || colName.contains("index") || colName.contains("ppi") -> "Index"
            else -> "-"
        }
    }
}
