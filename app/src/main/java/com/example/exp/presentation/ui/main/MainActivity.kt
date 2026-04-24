package com.example.exp.presentation.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.exp.data.local.db.AppDatabase
import com.example.exp.data.local.entity.RawEventEntity
import com.example.exp.data.repository.RawEventRepository
import com.example.exp.data.source.parser.SimpleSmsParser
import com.example.exp.domain.processor.RawEventProcessor
import com.example.exp.presentation.MainViewModel
import com.example.exp.ui.theme.ExpTheme
import java.util.UUID
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🔧 Manual dependency creation (temporary)
        val db = AppDatabase.getInstance(applicationContext)
        val rawEventDao = db.rawEventDao()
        val transactionDao = db.transactionDao()

        val repository = RawEventRepository(rawEventDao)
        val parser = SimpleSmsParser()

        val processor = RawEventProcessor(
            repository = repository,
            transactionDao = transactionDao,
            parser = parser
        )

        val viewModel = MainViewModel(processor)

        // 🧪 Insert test data (ONLY FOR NOW)
        runBlocking(Dispatchers.IO) {
            rawEventDao.insert(
                RawEventEntity(
                    id = UUID.randomUUID().toString(),
                    rawText = "Rs 500 spent on Swiggy",
                    sender = "HDFC", // ✅ add this
                    source = "SMS",
                    eventTime = System.currentTimeMillis(), // ✅ add this
                    receivedAt = System.currentTimeMillis(),
                    processed = false,
                    sourceId = "test_src_id_1"
                )
            )
        }

        setContent {
            ExpTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onRunPipeline = {

                            // 🔹 Run everything in background
                            lifecycleScope.launch(Dispatchers.IO) {

                                // 1. Insert test event
                                rawEventDao.insert(
                                    RawEventEntity(
                                        id = UUID.randomUUID().toString(),
                                        rawText = "Rs 500 spent on Swiggy",
                                        sender = "HDFC",
                                        source = "SMS",
                                        eventTime = System.currentTimeMillis(),
                                        receivedAt = System.currentTimeMillis(),
                                        processed = false,
                                        sourceId = "test_src_id_1"
                                    )
                                )

                                // 2. Run pipeline
                                viewModel.runPipeline()
                            }
                        }
                    )
                }
            }
        }


    }
}
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onRunPipeline: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onRunPipeline,
            modifier = Modifier
                .wrapContentSize()
        ) {
            Text("Run Pipeline")
        }
    }
}
