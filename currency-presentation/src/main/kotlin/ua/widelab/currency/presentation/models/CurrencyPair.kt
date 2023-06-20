package ua.widelab.currency.presentation.models

import androidx.compose.runtime.Stable

@Stable
data class CurrencyPair(
    val fromCurrency: Currency,
    val toCurrency: Currency
)