package com.econometrics

import com.econometrics.ingestion.DataIngestionService
import com.econometrics.model.DatasetSource
import com.econometrics.model.DatasetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DataIngestionTest {

    @Test
    fun testFredDataIngestion() {
        val fredData = DataIngestionService.fetchFredData(nPeriods = 120)
        assertEquals("FRED St. Louis Fed Macroeconomic Panel", fredData.title)
        assertEquals(DatasetSource.FRED_ST_LOUIS_FED, fredData.source)
        assertEquals(DatasetType.TIME_SERIES_MACRO, fredData.datasetType)
        assertEquals(120, fredData.sampleSize)
        assertTrue(fredData.columns.containsKey("gdp"))
        assertTrue(fredData.columns.containsKey("cpiaucsl"))
        assertTrue(fredData.columns.containsKey("unrate"))
        assertTrue(fredData.columns.containsKey("fedfunds"))
        assertTrue(fredData.columns.containsKey("m2sl"))
    }

    @Test
    fun testDataGovCensusIngestion() {
        val censusData = DataIngestionService.fetchDataGovCensus(nPeriods = 100)
        assertEquals(DatasetSource.DATA_GOV_CENSUS, censusData.source)
        assertEquals(100, censusData.sampleSize)
        assertTrue(censusData.columns.containsKey("retail_sales"))
        assertTrue(censusData.columns.containsKey("housing_starts"))
    }

    @Test
    fun testDataGovBlsIngestion() {
        val blsData = DataIngestionService.fetchDataGovBls(nPeriods = 100)
        assertEquals(DatasetSource.DATA_GOV_BLS, blsData.source)
        assertEquals(100, blsData.sampleSize)
        assertTrue(blsData.columns.containsKey("nonfarm_payrolls"))
        assertTrue(blsData.columns.containsKey("hourly_earnings"))
    }

    @Test
    fun testWorldBankIngestion() {
        val wbData = DataIngestionService.fetchWorldBankData(nCountries = 50)
        assertEquals(DatasetSource.WORLD_BANK_OPEN_DATA, wbData.source)
        assertEquals(DatasetType.CROSS_SECTIONAL, wbData.datasetType)
        assertEquals(50, wbData.sampleSize)
        assertTrue(wbData.columns.containsKey("gdp_per_capita"))
    }

    @Test
    fun testYahooFinanceIngestion() {
        val yahooData = DataIngestionService.fetchYahooFinanceMarketData(nPeriods = 100)
        assertEquals(DatasetSource.YAHOO_FINANCE_MARKET_DATA, yahooData.source)
        assertEquals(100, yahooData.sampleSize)
        assertTrue(yahooData.columns.containsKey("sp500_close"))
    }

    @Test
    fun testEiaEnergyIngestion() {
        val eiaData = DataIngestionService.fetchEiaEnergyData(nPeriods = 100)
        assertEquals(DatasetSource.EIA_ENERGY_ADMINISTRATION, eiaData.source)
        assertEquals(100, eiaData.sampleSize)
        assertTrue(eiaData.columns.containsKey("crude_oil_stocks"))
    }

    @Test
    fun testDragAndDropCsvParser() {
        val rawCsv = """
            date,gdp_usd,cpi_index,fed_funds_rate
            2024-01-01,27938.8,308.4,5.33
            2024-02-01,28010.2,309.2,5.33
            2024-03-01,28100.5,310.1,5.33
        """.trimIndent()

        val parsed = DataIngestionService.parseDraggedFile("my_custom_macro.csv", rawCsv)
        assertEquals("my_custom_macro", parsed.title)
        assertEquals(3, parsed.sampleSize)
        assertEquals(DatasetSource.USER_DRAG_AND_DROP_CSV, parsed.source)
        assertTrue(parsed.columns.containsKey("gdp_usd"))
        assertTrue(parsed.columns.containsKey("cpi_index"))
    }
}
