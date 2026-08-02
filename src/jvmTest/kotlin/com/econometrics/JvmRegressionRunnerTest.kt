package com.econometrics

import com.econometrics.engine.JvmRegressionRunner
import com.econometrics.ingestion.DataIngestionService
import com.econometrics.model.GenericEconometricEngine
import com.econometrics.model.ModelEstimator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JvmRegressionRunnerTest {

    @Test
    fun testPooledOlsEstimation() {
        val dataset = GenericEconometricEngine.generateSyntheticPanelDataset()
        val ols = JvmRegressionRunner.runPooledOls(dataset)

        assertEquals(ModelEstimator.POOLED_OLS_HC3, ols.estimator)
        assertEquals("Pooled OLS (HC3)", ols.estimatorName)
        assertTrue(ols.rSquared in 0.0..1.0)
        assertTrue(ols.coefficients.containsKey("log_price_usd"))
        
        val priceCoef = ols.coefficients["log_price_usd"]!!
        assertTrue(priceCoef < 0.0, "Demand elasticity log price coefficient should be negative")
    }

    @Test
    fun testFixedEffectsEstimation() {
        val dataset = GenericEconometricEngine.generateSyntheticPanelDataset()
        val fe = JvmRegressionRunner.runFixedEffects(dataset)

        assertEquals(ModelEstimator.FIXED_EFFECTS_FE, fe.estimator)
        assertEquals("Fixed Effects (FE)", fe.estimatorName)
        assertTrue(fe.coefficients.containsKey("log_price_usd"))

        val fePriceCoef = fe.coefficients["log_price_usd"]!!
        assertTrue(fePriceCoef < 0.0, "FE elasticity coefficient should be negative")
    }

    @Test
    fun testTwoStageLeastSquaresIvEstimation() {
        val dataset = GenericEconometricEngine.generateSyntheticPanelDataset()
        val iv = JvmRegressionRunner.run2SlsIv(dataset)

        assertEquals(ModelEstimator.TWO_STAGE_LEAST_SQUARES_IV, iv.estimator)
        assertEquals("2SLS IV (Causal)", iv.estimatorName)
        assertTrue(iv.coefficients.containsKey("log_price_usd"))

        val ivPriceCoef = iv.coefficients["log_price_usd"]!!
        assertTrue(ivPriceCoef < 0.0, "2SLS IV causal elasticity coefficient should be negative")
    }

    @Test
    fun testFredRegressionEstimation() {
        val fredData = DataIngestionService.fetchFredData()
        val ols = JvmRegressionRunner.runPooledOls(fredData)
        assertNotNull(ols)
        assertTrue(ols.rSquared > 0.5)
    }
}
