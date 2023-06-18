package ua.widelab.currency.entities.models

data class ExchangeWithCurrency(
    val exchangeEntity: Exchange?,
    val toCurrency: Currency,
    val fromCurrency: Currency,
)