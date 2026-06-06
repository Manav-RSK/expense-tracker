package com.example.exp.presentation.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.exp.domain.permission.PermissionManager
import com.example.exp.domain.permission.getAllPermissions
import com.example.exp.presentation.screen.permission.PermissionsScreen
import com.example.exp.presentation.screen.transaction.TransactionUiState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.exp.data.local.db.AppDatabase
import com.example.exp.data.local.entity.RawEventEntity
import com.example.exp.data.repository.RawEventRepository
import com.example.exp.data.source.parser.SimpleSmsParser
import com.example.exp.domain.classifier.TransactionClassifier
import com.example.exp.domain.contact.ContactMatcher
import com.example.exp.domain.history.HistoryMatcher
import com.example.exp.domain.processor.RawEventProcessor
import com.example.exp.presentation.MainViewModel
import com.example.exp.presentation.screen.transaction.TransactionScreen
import com.example.exp.ui.theme.ExpTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val testMessages = listOf(
            "Rs 1000 sent to Aman Raj",
            "Paid Rs 250 to abc@oksbi",
            "Rs 700 paid to Rakesh",
            "Rs 1200 spent on Amazon"
        )

        // 🔧 Dependencies
        val db = AppDatabase.getInstance(applicationContext)
        val rawEventDao = db.rawEventDao()
        val transactionDao = db.transactionDao()

        val classifier = TransactionClassifier()
        val contactMatcher = ContactMatcher()
        val historyMatcher = HistoryMatcher(transactionDao)
        val repository = RawEventRepository(rawEventDao)
        val rawEventsViewModel = com.example.exp.presentation.RawEventsViewModel(repository)
        val parser = SimpleSmsParser()

        val processor = RawEventProcessor(
            repository = repository,
            transactionDao = transactionDao,
            parser = parser,
            classifier = classifier,
            contactMatcher = contactMatcher,
            historyMatcher = historyMatcher
        )

        val viewModel = MainViewModel(processor, transactionDao, repository)

        val clearDatabase: () -> Unit = {
            lifecycleScope.launch(Dispatchers.IO) {
                rawEventDao.clearAll()
                transactionDao.clearAll()
            }
            Unit   // 🔥 force return type
        }

        val permissionManager = PermissionManager(this)
        val allGranted = getAllPermissions().all { permissionManager.permissionChecker.checkPermissionStatus(it) }
        val initialShowPermissions = !allGranted

        setContent {
            ExpTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var showPermissions by remember { mutableStateOf(initialShowPermissions) }

                    if (showPermissions) {
                        PermissionsScreen(
                            activity = this@MainActivity,
                            permissionManager = permissionManager,
                            onContinue = {
                                showPermissions = false
                            }
                        )
                    } else {
                        MainScreen(
                            modifier = Modifier.padding(innerPadding),
                            onRunPipeline = {

                            // 🔹 Run everything in background
                            lifecycleScope.launch(Dispatchers.IO) {

                                // 1. Insert test event
                                rawEventDao.insert(
                                    RawEventEntity(
                                        id = UUID.randomUUID().toString(),
                                        rawText = testMessages.random(),
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
                        },

                            onClearDb = clearDatabase, // ✅ FIXED
                            transactionsFlow = viewModel.transactions,
                            rawEventsFlow = rawEventsViewModel.rawEventsFlow
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onRunPipeline: () -> Unit,
    onClearDb: () -> Unit,
    transactionsFlow: Flow<List<TransactionUiState>>,
    rawEventsFlow: kotlinx.coroutines.flow.Flow<List<com.example.exp.data.local.entity.RawEventEntity>>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TransactionScreen(modifier = modifier.weight(1f), onRunPipeline = onRunPipeline, onClearDb = onClearDb, transactionsFlow = transactionsFlow)
        // Small debug/raw-events view below transactions to show realtime DB inserts
        com.example.exp.presentation.screen.rawevent.RawEventsScreen(modifier = Modifier.fillMaxWidth().weight(0.4f), rawEventsFlow = rawEventsFlow)
    }
}