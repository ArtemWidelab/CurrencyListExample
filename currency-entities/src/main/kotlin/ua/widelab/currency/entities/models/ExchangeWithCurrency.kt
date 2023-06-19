package ua.widelab.currency.entities.models

data class ExchangeWithCurrency(
    val exchange: Exchange?,
    val toCurrency: Currency,
    val fromCurrency: Currency,
)