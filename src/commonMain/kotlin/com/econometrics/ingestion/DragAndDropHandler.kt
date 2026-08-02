package com.econometrics.ingestion

import com.econometrics.model.DatasetSource
import com.econometrics.model.TabularDataset

fun interface DragAndDropListener {
    fun onDatasetLoaded(dataset: TabularDataset)
}

object DragAndDropHandler {

    private val listeners = mutableListOf<DragAndDropListener>()

    fun registerListener(listener: DragAndDropListener) {
        listeners.add(listener)
    }

    fun handleDroppedFilePayload(fileName: String, fileContent: String) {
        val source = when {
            fileName.endsWith(".json", ignoreCase = true) -> DatasetSource.USER_DRAG_AND_DROP_JSON
            fileName.endsWith(".parquet", ignoreCase = true) -> DatasetSource.USER_DRAG_AND_DROP_PARQUET
            else -> DatasetSource.USER_DRAG_AND_DROP_CSV
        }

        val parsedDataset = DataIngestionService.parseDraggedFile(fileName, fileContent, source)
        listeners.forEach { it.onDatasetLoaded(parsedDataset) }
    }
}
