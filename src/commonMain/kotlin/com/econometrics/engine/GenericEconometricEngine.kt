package com.econometrics.model

import com.econometrics.model.TabularDataset
import com.econometrics.model.RegressionOutput
import com.econometrics.model.ModelEstimator
import kotlin.math.abs
import kotlin.math.exp

object GenericEconometricEngine {

    fun generateSyntheticPanelDataset(
        nEntities: Int = 10,
        nPeriods: Int = 100,
        seed: Long = 42L
    ): TabularDataset {
        val n = nEntities * nPeriods
        val logPrices = DoubleArray(n)
        val pricesUsd = DoubleArray(n)
        val pricesLocal = DoubleArray(n)
        val logQuantities = DoubleArray(n)
        val quantities = DoubleArray(n)
        val highDemandDummies = DoubleArray(n)
        val compPrices = DoubleArray(n)
        val logCompPrices = DoubleArray(n)
        val ratings = DoubleArray(n)
        val wholesaleCosts = DoubleArray(n)
        val logisticsCosts = DoubleArray(n)

        val rand = java.util.Random(seed)
        val basePrices = doubleArrayOf(51.77, 53.74, 50.10, 54.23, 47.82, 45.00, 48.50, 52.10, 55.00, 49.20)

        var idx = 0
        for (i in 1..nEntities) {
            val baseP = if (i <= basePrices.size) basePrices[i - 1] else (45.0 + i * 3.5)
            val alphaI = rand.nextGaussian() * 0.4
            for (t in 1..nPeriods) {
                val wholesale = 20.0 + rand.nextDouble() * 30.0 + rand.nextGaussian() * 2.0
                val logistics = 15.0 + 0.1 * t + rand.nextGaussian() * 3.0
                val logP = 0.35 * kotlin.math.ln(wholesale) + 0.22 * kotlin.math.ln(logistics) + 0.20 * alphaI + 0.005 * t + (kotlin.math.ln(baseP) + rand.nextGaussian() * 0.12)
                val pUsd = exp(logP)
                val compP = baseP * exp(rand.nextGaussian() * 0.08)
                val rating = kotlin.math.min(5.0, kotlin.math.max(1.0, 4.0 + 0.25 * alphaI + rand.nextGaussian() * 0.25))

                val logQ = 5.5 - 1.48 * logP + 0.55 * kotlin.math.ln(compP) + 0.45 * rating + alphaI + rand.nextGaussian() * 0.18
                val qUnits = exp(logQ)

                logPrices[idx] = logP
                pricesUsd[idx] = pUsd
                pricesLocal[idx] = pUsd * 0.92
                logQuantities[idx] = logQ
                quantities[idx] = qUnits
                compPrices[idx] = compP
                logCompPrices[idx] = kotlin.math.ln(compP)
                ratings[idx] = rating
                wholesaleCosts[idx] = wholesale
                logisticsCosts[idx] = logistics
                idx++
            }
        }

        val medianQ = quantities.sorted().let {
            if (it.size % 2 == 0) (it[it.size / 2 - 1] + it[it.size / 2]) / 2.0 else it[it.size / 2]
        }
        for (k in 0 until n) {
            highDemandDummies[k] = if (quantities[k] > medianQ) 1.0 else 0.0
        }

        val colMap = mapOf(
            "price_local" to DataColumn(ColumnMetadata("Local Price", "Local (£/€/$)"), pricesLocal),
            "price_usd" to DataColumn(ColumnMetadata("Price (USD)", "$ USD", isEndogenous = true), pricesUsd),
            "log_price_usd" to DataColumn(ColumnMetadata("log(Price [USD])", "$ USD", isEndogenous = true), logPrices),
            "quantity_units" to DataColumn(ColumnMetadata("Quantity Demanded", "# Units Sold", isDependent = true), quantities),
            "log_quantity" to DataColumn(ColumnMetadata("log(Quantity)", "# Units Sold", isDependent = true), logQuantities),
            "high_demand_dummy" to DataColumn(ColumnMetadata("High Demand Dummy", "Binary (0/1)", isDependent = true), highDemandDummies),
            "competitor_price_usd" to DataColumn(ColumnMetadata("Competitor Price", "$ USD"), compPrices),
            "log_competitor_price_usd" to DataColumn(ColumnMetadata("log(CompetitorPrice)", "$ USD"), logCompPrices),
            "rating_stars" to DataColumn(ColumnMetadata("Rating (Stars)", "Stars (1-5)"), ratings),
            "wholesale_cost_index" to DataColumn(ColumnMetadata("Wholesale Cost Index", "$ Index", isInstrument = true), wholesaleCosts),
            "logistics_cost_index" to DataColumn(ColumnMetadata("Logistics Cost Index", "$/Ton", isInstrument = true), logisticsCosts)
        )

        return TabularDataset(
            title = "Product Tracker Panel Dataset (N=10, T=100)",
            source = DatasetSource.PRODUCT_TRACKER_SCRAPED,
            datasetType = DatasetType.PANEL_DATA,
            entityColumn = "product_id",
            timeColumn = "period",
            columns = colMap
        )
    }
}
