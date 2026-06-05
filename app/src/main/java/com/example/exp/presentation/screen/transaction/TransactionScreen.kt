package com.example.exp.presentation.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.exp.presentation.component.TransactionCard
import kotlinx.coroutines.flow.Flow

@Composable
fun TransactionScreen(
	modifier: Modifier = Modifier,
	onRunPipeline: () -> Unit,
	onClearDb: () -> Unit,
	transactionsFlow: Flow<List<TransactionUiState>>
) {
	val transactionsState by transactionsFlow.collectAsState(initial = emptyList())

	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		// Header
		Text(text = "Transaction Intelligence", style = MaterialTheme.typography.headlineSmall)

		Spacer(modifier = Modifier.height(12.dp))

		// Action buttons
		Button(onClick = onRunPipeline) {
			Text("Run Pipeline")
		}

		Spacer(modifier = Modifier.height(8.dp))

		Button(
			onClick = onClearDb,
			colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
		) {
			Text("Clear DB")
		}

		Spacer(modifier = Modifier.height(20.dp))

		// Transactions list using LazyColumn for lazy loading
		LazyColumn(modifier = Modifier.fillMaxWidth()) {
			items(transactionsState) { tx ->
				TransactionCard(tx = tx)
			}
		}
	}
}