package ua.widelab.currency.repo

import com.github.kittinunf.result.Result
import kotlinx.coroutines.flow.Flow
import ua.widelab.currency.entities.models.Currency
import ua.widelab.currency.entities.models.CurrencyPair
import ua.widelab.currency.entities.models.ExchangeWithCurrency

interface CurrencyRepo {

    fun getCurrencies(): Flow<EndpointResult<List<Currency>>>
    fun getCurrencyPairsWithRates(): Flow<List<EndpointResult<ExchangeWithCurrency>>>
    suspend fun addCurrencyPair(from: Currency, to: Currency): Result<Unit, Throwable>
    suspend fun deleteCurrencyPair(currencyPair: CurrencyPair): Result<Unit, Throwable>
    suspend fun addDefaultCurrencyPairs()
}

