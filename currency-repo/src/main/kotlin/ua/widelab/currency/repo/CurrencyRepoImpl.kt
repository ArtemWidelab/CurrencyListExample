package ua.widelab.currency.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import ua.widelab.currency.api.CurrencyApiDataSource
import ua.widelab.currency.entities.models.Currency
import ua.widelab.currency.entities.models.CurrencyPair
import ua.widelab.currency.entities.models.Exchange
import ua.widelab.currency.entities.models.ExchangeWithCurrency
import ua.widelab.currency.persistence.CurrencyPersistenceDataSource
import ua.widelab.currency.persistence.models.entities.CurrencyID
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
        val CURRENCY_PAIRS_DATA_INITIATED = booleanPreferencesKey("currency_pairs_data_initiated")
    }

    override suspend fun addDefaultCurrencyPairs() {
        dataStore.data
            .mapLatest { it[CURRENCY_PAIRS_DATA_INITIATED] ?: false }
            .filter { !it }
            .flatMapLatest { persistenceDataSource.getCurrenciesList() }
            .mapLatest {
                listOfNotNull(
                    it.find("GBP"),
                    it.find("USD"),
                    it.find("EUR"),
                    it.find("UAH")
                )
            }
            .filter { it.size > 1 }
            .collectLatest { list ->
                list.drop(1).forEach {
                    addCurrencyPair(list.first(), it)
                }
                dataStore.edit {
                    it[CURRENCY_PAIRS_DATA_INITIATED] = true
                }
            }
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

    private fun List<Currency>.find(id: CurrencyID): Currency? {
        return this.firstOrNull { it.shortName == id }
    }

    override fun getCurrencyPairsWithRates(): Flow<List<EndpointResult<ExchangeWithCurrency>>> {
        return persistenceDataSource
            .getCurrencyPairs()
            .flatMapLatest {
                combine(
                    it.map {
                        flowOf(it)
                            .flatMapLatest { currencyPair ->
                                getRates(currencyPair).convert {
                                    ExchangeWithCurrency(
                                        exchange = it,
                                        toCurrency = currencyPair.toCurrency,
                                        fromCurrency = currencyPair.fromCurrency
                                    )
                                }
                            }
                    }
                ) {
                    it.toList()
                }
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