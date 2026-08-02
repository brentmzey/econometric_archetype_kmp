package com.econometrics

import com.econometrics.model.DatasetType
import com.econometrics.model.GenericEconometricEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EconometricEngineTest {

    @Test
    fun testSyntheticPanelDataGeneration() {
        val dataset = GenericEconometricEngine.generateSyntheticPanelDataset(nEntities = 10, nPeriods = 100)
        assertEquals("Product Tracker Panel Dataset (N=10, T=100)", dataset.title)
        assertEquals(DatasetType.PANEL_DATA, dataset.datasetType)
        assertEquals(1000, dataset.sampleSize)

        val logPrice = dataset.columns["log_price_usd"]
        assertNotNull(logPrice)
        assertEquals(1000, logPrice.values.size)
        assertTrue(logPrice.mean > 0.0)
        assertTrue(logPrice.stdDev > 0.0)

        val logQuantity = dataset.columns["log_quantity"]
        assertNotNull(logQuantity)
        assertEquals(1000, logQuantity.values.size)
    }
}
