package com.example.exp.presentation.screen.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.exp.presentation.util.sendTestNotification
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
			.padding(20.dp)
	) {

		// Header
		Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
			Text(
				text = "Transaction Intelligence",
				style = MaterialTheme.typography.headlineMedium,
				fontSize = 28.sp
			)

			Spacer(modifier = Modifier.height(18.dp))

			// Action buttons stacked
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				val btnModifier = Modifier
					.fillMaxWidth()
					.height(56.dp)
					.padding(horizontal = 32.dp)

				Button(
					onClick = onRunPipeline,
					modifier = btnModifier,
					shape = RoundedCornerShape(28.dp),
					colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
				) {
					Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
					Spacer(modifier = Modifier.width(12.dp))
					Text("Run Pipeline", color = Color.White)
				}

				Spacer(modifier = Modifier.height(12.dp))

				val context = LocalContext.current
				Button(
					onClick = { sendTestNotification(context) },
					modifier = btnModifier,
					shape = RoundedCornerShape(28.dp),
					colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
				) {
					Icon(imageVector = Icons.Default.MailOutline, contentDescription = null, tint = Color.White)
					Spacer(modifier = Modifier.width(12.dp))
					Text("Send Test Notification", color = Color.White)
				}

				Spacer(modifier = Modifier.height(12.dp))

				Button(
					onClick = onClearDb,
					modifier = btnModifier,
					shape = RoundedCornerShape(28.dp),
					colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
				) {
					Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White)
					Spacer(modifier = Modifier.width(12.dp))
					Text("Clear DB", color = Color.White)
				}
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		// Transactions list using LazyColumn for lazy loading. Give it weight so
		// the list expands and allows other UI (raw events) to be visible below.
		LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
			items(transactionsState) { tx ->
				TransactionCard(tx = tx)
			}
		}
	}
}