package com.example.exp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exp.data.local.dao.TransactionDao
import com.example.exp.domain.processor.RawEventProcessor
import com.example.exp.data.repository.RawEventRepository
import com.example.exp.presentation.screen.transaction.TransactionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainViewModel(
    private val processor: RawEventProcessor,
    private val transactionDao: TransactionDao? = null,
    private val repository: RawEventRepository? = null
) : ViewModel() {

    // Expose transactions as a flow of UI models. If DAO isn't provided, return empty list flow.
    val transactions: Flow<List<TransactionUiState>> = transactionDao?.getAllTransactions()
        ?.map { list ->
            list.map { entity ->
                TransactionUiState(
                    title = entity.merchantName,
                    amountDisplay = "₹${entity.amount.toInt()}",
                    kind = entity.type,
                    score = entity.confidenceScore
                )
            }
        } ?: flowOf(emptyList())

    fun runPipeline() {
        viewModelScope.launch(Dispatchers.IO) {
            processor.processBatch()
        }
    }

    init {
        // If a repository is available, automatically trigger processing when new
        // raw events are inserted. This enables near-realtime processing when the
        // NotificationCaptureService inserts rows.
        if (repository != null) {
            viewModelScope.launch {
                repository.newEvents().collect {
                    // run processing in IO
                    viewModelScope.launch(Dispatchers.IO) {
                        processor.processBatch()
                    }
                }
            }
        }
    }
}