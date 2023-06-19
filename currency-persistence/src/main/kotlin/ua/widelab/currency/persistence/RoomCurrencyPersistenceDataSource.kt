package ua.widelab.currency.persistence

import com.github.kittinunf.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import ua.widelab.currency.entities.models.Currency
import ua.widelab.currency.entities.models.CurrencyPair
import ua.widelab.currency.entities.models.Exchange
import ua.widelab.currency.entities.models.ExchangeWithCurrency
import ua.widelab.currency.persistence.db.CurrencyDao
import ua.widelab.currency.persistence.models.entities.CurrencyEntity
import ua.widelab.currency.persistence.models.entities.CurrencyEntity.Companion.toCurrency
import ua.widelab.currency.persistence.models.entities.CurrencyPairEntity
import ua.widelab.currency.persistence.models.entities.CurrencyPairValue.Companion.toCurrencyPair
import ua.widelab.currency.persistence.models.entities.ExchangeRateEntity
import ua.widelab.currency.persistence.models.entities.ExchangeRateEntity.Companion.toExchange
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
internal class RoomCurrencyPersistenceDataSource @Inject constructor(
    private val currencyDao: CurrencyDao
) : CurrencyPersistenceDataSource {

    override suspend fun updateCurrenciesList(currencies: List<Currency>) {
        currencyDao.deleteCurrencies(keepIds = currencies.map { it.shortName })
        currencyDao.insertAll(currencies.map { CurrencyEntity.fromCurrency(it) })
    }

    override fun getCurrenciesList(): Flow<List<Currency>> {
        return currencyDao.getAllCurrencies()
            .mapLatest { it.map { it.toCurrency() } }
    }

    override suspend fun addCurrencyPair(currencyPair: CurrencyPair): Result<Unit, AddCurrencyPairThrowable> {
        val isAlreadyCreated = currencyDao.getCurrencyPair(
            toCurrencyID = currencyPair.toCurrency.shortName,
            fromCurrencyID = currencyPair.fromCurrency.shortName
        ) != null
        if (isAlreadyCreated) return Result.failure(AddCurrencyPairThrowable.AlreadyCreated)
        currencyDao.insert(CurrencyPairEntity.fromCurrencyPair(currencyPair))
        return Result.success(Unit)
    }

    override suspend fun deleteCurrencyPair(from: Currency, to: Currency) {
        currencyDao.deleteCurrencyPair(
            toCurrencyID = from.shortName,
            fromCurrencyID = to.shortName
        )
    }

    override fun getCurrencyPairsWithRates(): Flow<List<ExchangeWithCurrency>> {
        return currencyDao.getCurrencyPairsWithRates()
            .mapLatest {
                it.map {
                    ExchangeWithCurrency(
                        exchange = it.value?.toExchange(),
                        toCurrency = it.key.toCurrency.toCurrency(),
                        fromCurrency = it.key.fromCurrency.toCurrency()
                    )
                }
            }
    }

    override fun getCurrencyPairs(): Flow<List<CurrencyPair>> {
        return currencyDao.getCurrencyPairs().mapLatest {
            it.map {
                it.toCurrencyPair()
            }
        }
    }

    override suspend fun addExchangeRate(
        from: Currency,
        to: Currency,
        exchange: Exchange
    ): Result<Unit, AddExchangeRateThrowable> {
        val currencyPair =
            currencyDao.getCurrencyPair(to.shortName, from.shortName) ?: return Result.failure(
                AddExchangeRateThrowable.NoSuchCurrencyPair
            )

        currencyDao.insert(
            ExchangeRateEntity.fromExchange(
                exchange = exchange,
                currencyPairId = currencyPair.uid
            )
        )
        return Result.success(Unit)
    }

    override fun getCurrencyRate(from: Currency, to: Currency): Flow<Exchange?> {
        return flowOf(CurrencyPair(fromCurrency = from, toCurrency = to))
            .mapLatest {
                currencyDao.getCurrencyPair(
                    fromCurrencyID = it.fromCurrency.shortName,
                    toCurrencyID = it.toCurrency.shortName
                )
            }
            .flatMapLatest {
                if (it == null) return@flatMapLatest flowOf(null)
                currencyDao.getCurrencyRate(it.uid)
            }
    }

}