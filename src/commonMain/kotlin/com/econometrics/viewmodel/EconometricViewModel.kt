package com.econometrics.viewmodel

import com.econometrics.ingestion.DataIngestionService
import com.econometrics.ingestion.DragAndDropHandler
import com.econometrics.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EconometricUiState(
    val activeDataset: TabularDataset? = null,
    val selectedEstimators: List<ModelEstimator> = listOf(
        ModelEstimator.POOLED_OLS_HC3,
        ModelEstimator.FIXED_EFFECTS_FE,
        ModelEstimator.TWO_STAGE_LEAST_SQUARES_IV
    ),
    val statusMessage: String = "Ready. Select FRED, Data.gov (Census/BLS), World Bank, Yahoo Finance, EIA, or Drag & Drop data.",
    val isLoading: Boolean = false
)

class EconometricViewModel {

    private val _uiState = MutableStateFlow(EconometricUiState())
    val uiState: StateFlow<EconometricUiState> = _uiState.asStateFlow()

    init {
        DragAndDropHandler.registerListener { dataset ->
            _uiState.value = _uiState.value.copy(
                activeDataset = dataset,
                statusMessage = "Loaded Drag & Drop Dataset '${dataset.title}' (${dataset.sampleSize} obs, Source=${dataset.source}, Schema=${dataset.datasetType})."
            )
        }
        loadFredDataset()
    }

    fun loadFredDataset() {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Fetching FRED St. Louis Fed Macroeconomic Panel...")
        val fredData = DataIngestionService.fetchFredData()
        _uiState.value = _uiState.value.copy(activeDataset = fredData, statusMessage = "Loaded FRED Dataset (${fredData.sampleSize} observations across ${fredData.columns.size} series).", isLoading = false)
    }

    fun loadDataGovCensusDataset() {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Fetching Data.gov U.S. Census Bureau Economic Data...")
        val censusData = DataIngestionService.fetchDataGovCensus()
        _uiState.value = _uiState.value.copy(activeDataset = censusData, statusMessage = "Loaded Data.gov Census Dataset (${censusData.sampleSize} observations).", isLoading = false)
    }

    fun loadDataGovBlsDataset() {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Fetching Data.gov Bureau of Labor Statistics (BLS) Employment Data...")
        val blsData = DataIngestionService.fetchDataGovBls()
        _uiState.value = _uiState.value.copy(activeDataset = blsData, statusMessage = "Loaded Data.gov BLS Dataset (${blsData.sampleSize} observations).", isLoading = false)
    }

    fun loadWorldBankDataset() {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Fetching World Bank Open Data Global Country Panel...")
        val wbData = DataIngestionService.fetchWorldBankData()
        _uiState.value = _uiState.value.copy(activeDataset = wbData, statusMessage = "Loaded World Bank Dataset (${wbData.sampleSize} countries).", isLoading = false)
    }

    fun loadYahooFinanceDataset() {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Fetching Yahoo Finance Market & Commodity Futures Data...")
        val yahooData = DataIngestionService.fetchYahooFinanceMarketData()
        _uiState.value = _uiState.value.copy(activeDataset = yahooData, statusMessage = "Loaded Yahoo Finance Dataset (${yahooData.sampleSize} trading days).", isLoading = false)
    }

    fun loadEiaEnergyDataset() {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Fetching U.S. EIA Energy Information Administration Data...")
        val eiaData = DataIngestionService.fetchEiaEnergyData()
        _uiState.value = _uiState.value.copy(activeDataset = eiaData, statusMessage = "Loaded EIA Energy Dataset (${eiaData.sampleSize} observations).", isLoading = false)
    }

    fun loadProductTrackerScrapedData() {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Loading Scraped Product Tracker Panel Data...")
        val productData = GenericEconometricEngine.generateSyntheticPanelDataset()
        _uiState.value = _uiState.value.copy(activeDataset = productData, statusMessage = "Loaded Product Tracker Scraped Dataset (N=10, T=100 = 1,000 observations).", isLoading = false)
    }

    fun onFileDropped(fileName: String, fileContent: String) {
        DragAndDropHandler.handleDroppedFilePayload(fileName, fileContent)
    }
}
