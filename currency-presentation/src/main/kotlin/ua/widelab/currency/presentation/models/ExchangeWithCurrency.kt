package ua.widelab.currency.presentation.models

data class ExchangeWithCurrency(
    val exchange: Exchange?,
    val toCurrency: Currency,
    val fromCurrency: Currency,
    val loading: Boolean,
    val error: Throwable?
) {
    companion object {
        fun fromRepoModel(
            exchangeWithCurrency: ua.widelab.currency.entities.models.ExchangeWithCurrency,
            loading: Boolean,
            error: Throwable?
        ): ExchangeWithCurrency {
            return ExchangeWithCurrency(
                exchange = exchangeWithCurrency.exchange?.let { Exchange.fromRepoModel(it) },
                toCurrency = Currency.fromRepoModel(exchangeWithCurrency.toCurrency),
                fromCurrency = Currency.fromRepoModel(exchangeWithCurrency.fromCurrency),
                loading = loading,
                error = error
            )
        }
    }

    fun toText(): String {
        if (exchange != null) {
            return "${exchange.amount.toPlainString()} ${fromCurrency.shortName} - ${exchange.rate.toPlainString()} ${toCurrency.shortName}"
        }
        return "${fromCurrency.shortName} - ${toCurrency.shortName}"
    }
}