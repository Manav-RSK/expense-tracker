package com.example.exp.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exp.presentation.screen.transaction.TransactionUiState

@Composable
fun TransactionCard(
	tx: TransactionUiState,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.surfaceVariant,
		tonalElevation = 2.dp
	) {
		Column(modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp)) {

			Text(text = tx.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

			Spacer(modifier = Modifier.height(6.dp))

			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(text = tx.amountDisplay, fontSize = 16.sp, fontWeight = FontWeight.Bold)

				Spacer(modifier = Modifier.width(12.dp))

				Column {
					Text(text = tx.kind, fontSize = 12.sp, color = Color.Gray)
					Text(text = "Score: ${tx.score}", fontSize = 12.sp, color = Color.Gray)
				}
			}
		}
	}
}