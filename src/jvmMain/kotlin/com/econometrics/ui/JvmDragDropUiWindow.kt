package com.econometrics.ui

import com.econometrics.engine.JvmRegressionRunner
import com.econometrics.ingestion.DataIngestionService
import com.econometrics.ingestion.DragAndDropHandler
import com.econometrics.model.TabularDataset
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.XYChartBuilder
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import javax.swing.*

class JvmDragDropUiWindow : JFrame("📱 KMM & Desktop Drag & Drop Econometric Platform") {

    private val statusLabel = JLabel("Ready. Drag & Drop CSV / JSON file below or pick a dataset.", SwingConstants.CENTER)
    private val outputArea = JTextArea()
    private val chartContainer = JPanel(BorderLayout())
    private var activeDataset: TabularDataset? = null

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(1200, 800)
        setLocationRelativeTo(null)
        layout = BorderLayout()

        // 1. Header Banner & Dataset Selector Toolbar
        val topPanel = JPanel(BorderLayout())
        val bannerLabel = JLabel("🏛️ KMM Multiplatform Drag & Drop Econometric Platform", SwingConstants.CENTER)
        bannerLabel.font = Font("SansSerif", Font.BOLD, 18)
        bannerLabel.foreground = Color(205, 214, 244)
        topPanel.background = Color(24, 24, 37)
        topPanel.add(bannerLabel, BorderLayout.NORTH)

        val btnPanel = JPanel(FlowLayout())
        btnPanel.background = Color(30, 30, 46)

        val btnFred = JButton("FRED Macro")
        val btnCensus = JButton("Data.gov Census")
        val btnBls = JButton("Data.gov BLS")
        val btnWb = JButton("World Bank")
        val btnYahoo = JButton("Yahoo Finance")
        val btnEia = JButton("EIA Energy")
        val btnScraped = JButton("Scraped Panel")

        btnFred.addActionListener { loadAndDisplayDataset(DataIngestionService.fetchFredData()) }
        btnCensus.addActionListener { loadAndDisplayDataset(DataIngestionService.fetchDataGovCensus()) }
        btnBls.addActionListener { loadAndDisplayDataset(DataIngestionService.fetchDataGovBls()) }
        btnWb.addActionListener { loadAndDisplayDataset(DataIngestionService.fetchWorldBankData()) }
        btnYahoo.addActionListener { loadAndDisplayDataset(DataIngestionService.fetchYahooFinanceMarketData()) }
        btnEia.addActionListener { loadAndDisplayDataset(DataIngestionService.fetchEiaEnergyData()) }
        btnScraped.addActionListener { loadAndDisplayDataset(com.econometrics.model.GenericEconometricEngine.generateSyntheticPanelDataset()) }

        btnPanel.add(btnFred)
        btnPanel.add(btnCensus)
        btnPanel.add(btnBls)
        btnPanel.add(btnWb)
        btnPanel.add(btnYahoo)
        btnPanel.add(btnEia)
        btnPanel.add(btnScraped)
        topPanel.add(btnPanel, BorderLayout.SOUTH)

        add(topPanel, BorderLayout.NORTH)

        // 2. Drag & Drop Visual Target Dropzone Area
        val dropZone = JPanel(GridBagLayout())
        dropZone.preferredSize = Dimension(1200, 100)
        dropZone.background = Color(49, 50, 68)
        dropZone.border = BorderFactory.createDashedBorder(Color(137, 180, 250), 2.0f, 5.0f)

        val dropLabel = JLabel("📂 DRAG & DROP CSV / JSON / TSV FILES HERE FOR AUTOMATIC ECONOMETRIC REGRESSION")
        dropLabel.font = Font("SansSerif", Font.BOLD, 14)
        dropLabel.foreground = Color(137, 180, 250)
        dropZone.add(dropLabel)

        setupDragAndDropTarget(dropZone)

        // 3. Main Center Split (Text Benchmark Output + XChart Graphic)
        outputArea.font = Font("Monospaced", Font.PLAIN, 12)
        outputArea.background = Color(17, 17, 27)
        outputArea.foreground = Color(205, 214, 244)
        outputArea.isEditable = false

        val scrollText = JScrollPane(outputArea)
        chartContainer.background = Color(24, 24, 37)

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollText, chartContainer)
        splitPane.dividerLocation = 600

        val centerPanel = JPanel(BorderLayout())
        centerPanel.add(dropZone, BorderLayout.NORTH)
        centerPanel.add(splitPane, BorderLayout.CENTER)

        add(centerPanel, BorderLayout.CENTER)

        // 4. Status Bar
        statusLabel.isOpaque = true
        statusLabel.background = Color(30, 30, 46)
        statusLabel.foreground = Color(166, 227, 161)
        add(statusLabel, BorderLayout.SOUTH)

        // Load Default Dataset
        loadAndDisplayDataset(DataIngestionService.fetchFredData())
    }

    private fun setupDragAndDropTarget(panel: JPanel) {
        DropTarget(panel, object : DropTargetAdapter() {
            override fun drop(dtde: DropTargetDropEvent) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY)
                    val droppedFiles = dtde.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    val firstFile = droppedFiles.firstOrNull() as? File
                    if (firstFile != null && firstFile.isFile) {
                        val text = firstFile.readText()
                        val dataset = DataIngestionService.parseDraggedFile(firstFile.name, text)
                        loadAndDisplayDataset(dataset)
                    }
                } catch (e: Exception) {
                    statusLabel.text = "Error loading dropped file: ${e.message}"
                }
            }
        })
    }

    fun loadAndDisplayDataset(dataset: TabularDataset) {
        activeDataset = dataset
        statusLabel.text = "Loaded Dataset: '${dataset.title}' (${dataset.sampleSize} obs, Source=${dataset.source}, Schema=${dataset.datasetType})"

        val ols = JvmRegressionRunner.runPooledOls(dataset)
        val iv = JvmRegressionRunner.run2SlsIv(dataset)

        val report = buildString {
            appendLine("=== DATASET: ${dataset.title.uppercase()} ===")
            appendLine("Source: ${dataset.source}")
            appendLine("Schema: ${dataset.datasetType}")
            appendLine("Sample Size: ${dataset.sampleSize} observations")
            appendLine()
            appendLine("--- DESCRIPTIVE STATISTICS ---")
            dataset.columns.forEach { (key, col) ->
                appendLine("%-25s | Mean: %10.4f | StdDev: %10.4f | Min: %10.4f | Max: %10.4f".format(col.metadata.name, col.mean, col.stdDev, col.min, col.max))
            }
            appendLine()
            appendLine("--- POOLED OLS REGRESSION ---")
            appendLine("R-Squared: ${String.format("%.4f", ols.rSquared)}")
            appendLine("Intercept: ${String.format("%.4f", ols.intercept)}")
            ols.coefficients.forEach { (varKey, coef) ->
                appendLine("Coef [%-20s]: %10.4f (t = %7.4f, p = %7.4f)".format(varKey, coef, ols.tStatistics[varKey] ?: 0.0, ols.pValues[varKey] ?: 0.0))
            }
            appendLine()
            appendLine("--- 2SLS IV CAUSAL REGRESSION ---")
            appendLine("R-Squared: ${String.format("%.4f", iv.rSquared)}")
            appendLine("Intercept: ${String.format("%.4f", iv.intercept)}")
            iv.coefficients.forEach { (varKey, coef) ->
                appendLine("Coef [%-20s]: %10.4f (t = %7.4f, p = %7.4f)".format(varKey, coef, iv.tStatistics[varKey] ?: 0.0, iv.pValues[varKey] ?: 0.0))
            }
        }

        outputArea.text = report
        updateChartPanel(dataset)
    }

    private fun updateChartPanel(dataset: TabularDataset) {
        chartContainer.removeAll()

        val yCol = dataset.columns.values.firstOrNull { it.metadata.isDependent } ?: dataset.columns.values.first()
        val xCol = dataset.columns.values.firstOrNull { it != yCol } ?: dataset.columns.values.elementAt(1)

        val chart = XYChartBuilder()
            .width(600)
            .height(500)
            .title("${dataset.title}: ${yCol.metadata.name} vs ${xCol.metadata.name}")
            .xAxisTitle(xCol.metadata.name)
            .yAxisTitle(yCol.metadata.name)
            .build()

        chart.styler.chartBackgroundColor = Color(24, 24, 37)
        chart.styler.plotBackgroundColor = Color(30, 30, 46)
        chart.styler.chartFontColor = Color(205, 214, 244)
        chart.styler.axisTickLabelsColor = Color(205, 214, 244)

        val series = chart.addSeries("Observations (${dataset.sampleSize})", xCol.values, yCol.values)
        series.lineStyle = org.knowm.xchart.style.lines.SeriesLines.NONE
        series.markerColor = Color(243, 139, 168)

        val chartPanel = XChartPanel(chart)
        chartContainer.add(chartPanel, BorderLayout.CENTER)
        chartContainer.revalidate()
        chartContainer.repaint()
    }

    companion object {
        fun launchGui() {
            SwingUtilities.invokeLater {
                val window = JvmDragDropUiWindow()
                window.isVisible = true
            }
        }
    }
}
