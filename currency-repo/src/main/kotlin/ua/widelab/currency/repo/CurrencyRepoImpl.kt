package ua.widelab.currency.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import ua.widelab.currency.api.CurrencyApiDataSource
import ua.widelab.currency.entities.models.Currency
import ua.widelab.currency.entities.models.CurrencyPair
import ua.widelab.currency.entities.models.Exchange
import ua.widelab.currency.entities.models.ExchangeWithCurrency
import ua.widelab.currency.persistence.CurrencyPersistenceDataSource
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

internal class CurrencyRepoImpl @Inject constructor(
    private val persistenceDataSource: CurrencyPersistenceDataSource,
    private val apiDataSource: CurrencyApiDataSource,
    private val dataStore: DataStore<Preferences>
) : CurrencyRepo {

    companion object {
        val DEFAULT_AMOUNT: BigDecimal = BigDecimal.ONE
        val LAST_CURRENCIES_REQUEST = longPreferencesKey("last_currencies_request")
    }

    override fun getCurrencies(): Flow<EndpointResult<List<Currency>>> {
        return object : Endpoint<List<Currency>, List<Currency>>() {
            override val cache: Flow<List<Currency>> =
                persistenceDataSource.getCurrenciesList()
            override val network: Flow<Result<List<Currency>, Throwable>> =
                flow { emit(apiDataSource.getCurrencies()) }
                    .onEach {
                        if (it.isSuccess()) {
                            dataStore.edit {
                                it[LAST_CURRENCIES_REQUEST] = System.currentTimeMillis()
                            }
                        }
                    }

            override suspend fun shouldMakeRequest(cache: List<Currency>): Boolean {
                return Instant.ofEpochMilli(dataStore.data.first()[LAST_CURRENCIES_REQUEST] ?: 0)
                    .isBefore(Instant.now().minusMillis(1.days.inWholeMilliseconds))
            }

            override suspend fun store(data: List<Currency>) {
                persistenceDataSource.updateCurrenciesList(data)
            }

        }.get()
    }

    override fun getCurrencyPairsWithRates(): Flow<List<EndpointResult<ExchangeWithCurrency>>> {
        return persistenceDataSource
            .getCurrencyPairs()
            .flatMapLatest {
                flowOf(*it.toTypedArray())
                    .flatMapMerge { currencyPair ->
                        getRates(currencyPair)
                            .convert {
                                ExchangeWithCurrency(
                                    exchange = it,
                                    toCurrency = currencyPair.toCurrency,
                                    fromCurrency = currencyPair.fromCurrency
                                )
                            }
                            .mapLatest {
                                CurrencyPairWrapper(
                                    currencyPair = currencyPair,
                                    endpointResult = it
                                )
                            }
                    }
                    .runningFold(CurrencyPairsListAccumulatorWrapper()) { accumulator, value ->
                        accumulator.add(value)
                        accumulator
                    }
                    .mapLatest { it.getList() }
            }
    }

    private fun getRates(
        currencyPair: CurrencyPair
    ): Flow<EndpointResult<Exchange?>> {
        return object : Endpoint<Exchange?, Exchange>() {
            override val cache: Flow<Exchange?>
                get() = persistenceDataSource.getCurrencyRate(
                    from = currencyPair.fromCurrency,
                    to = currencyPair.toCurrency
                )
            override val network: Flow<Result<Exchange, Throwable>>
                get() = flow {
                    emit(
                        apiDataSource.getExchangeRate(
                            from = currencyPair.fromCurrency,
                            to = currencyPair.toCurrency,
                            amount = DEFAULT_AMOUNT
                        )
                    )
                }

            override suspend fun shouldMakeRequest(cache: Exchange?): Boolean {
                return cache?.date?.isBefore(LocalDate.now()) ?: true
            }

            override suspend fun store(data: Exchange) {
                persistenceDataSource.addExchangeRate(
                    from = currencyPair.fromCurrency,
                    to = currencyPair.toCurrency,
                    exchange = data
                )
            }
        }.get()
    }

    override suspend fun addCurrencyPair(from: Currency, to: Currency): Result<Unit, Throwable> {
        return persistenceDataSource.addCurrencyPair(
            CurrencyPair(
                fromCurrency = from,
                toCurrency = to
            )
        )
    }

    override suspend fun deleteCurrencyPair(currencyPair: CurrencyPair): Result<Unit, Throwable> {
        return Result.of {
            persistenceDataSource.deleteCurrencyPair(
                from = currencyPair.fromCurrency,
                to = currencyPair.toCurrency
            )
        }
    }
}

private data class CurrencyPairWrapper(
    val currencyPair: CurrencyPair,
    val endpointResult: EndpointResult<ExchangeWithCurrency>
)

private class CurrencyPairsListAccumulatorWrapper {
    private val data = mutableMapOf<CurrencyPair, EndpointResult<ExchangeWithCurrency>>()

    fun add(currencyPairWrapper: CurrencyPairWrapper) {
        data[currencyPairWrapper.currencyPair] = currencyPairWrapper.endpointResult
    }

    fun getList(): List<EndpointResult<ExchangeWithCurrency>> {
        return data.values.toList()
    }
}