package ua.widelab.currency.persistence

import com.github.kittinunf.result.Result
import kotlinx.coroutines.flow.Flow
import ua.widelab.currency.entities.models.Currency
import ua.widelab.currency.entities.models.CurrencyPair
import ua.widelab.currency.entities.models.Exchange
import ua.widelab.currency.entities.models.ExchangeWithCurrency

interface CurrencyPersistenceDataSource {
    suspend fun updateCurrenciesList(currencies: List<Currency>)
    fun getCurrenciesList(): Flow<List<Currency>>
    suspend fun addCurrencyPair(currencyPair: CurrencyPair): Result<Unit, AddCurrencyPairThrowable>
    suspend fun deleteCurrencyPair(from: Currency, to: Currency)
    fun getCurrencyPairsWithRates(): Flow<List<ExchangeWithCurrency>>
    suspend fun addExchangeRate(
        from: Currency,
        to: Currency,
        exchange: Exchange
    ): Result<Unit, AddExchangeRateThrowable>
}

sealed class AddCurrencyPairThrowable : Throwable() {
    object AlreadyCreated : AddCurrencyPairThrowable()
}

sealed class AddExchangeRateThrowable : Throwable() {
    object NoSuchCurrencyPair : AddExchangeRateThrowable()
}