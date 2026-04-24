package com.example.exp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exp.domain.processor.RawEventProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val processor: RawEventProcessor
) : ViewModel() {

    fun runPipeline() {
        viewModelScope.launch(Dispatchers.IO) {
            processor.processBatch()
        }
    }
}