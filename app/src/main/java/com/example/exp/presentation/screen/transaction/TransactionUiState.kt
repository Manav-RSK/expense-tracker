package com.example.exp.presentation.screen.transaction

import androidx.annotation.Keep

@Keep
data class TransactionUiState(
	val title: String,
	val amountDisplay: String,
	val kind: String, // e.g. MERCHANT or PERSON
	val score: Int
)
