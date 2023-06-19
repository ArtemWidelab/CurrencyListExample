package ua.widelab.currency.api

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import retrofit2.HttpException
import retrofit2.Response
import ua.widelab.currency.entities.models.Currency
import ua.widelab.currency.entities.models.Exchange
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

internal class CurrencyApiDataSourceImpl @Inject constructor(
    private val service: CurrencyService
) : CurrencyApiDataSource {

    private suspend fun <T> executeRequest(request: suspend () -> Response<T>): Result<T, CurrencyApiThrowable> {
        try {
            val response = request.invoke()
            if (response.isSuccessful && response.body() != null) {
                return Result.success(response.body()!!)
            }
            return Result.failure(CurrencyApiThrowable(HttpException(response)))
        } catch (e: Throwable) {
            return Result.failure(
                when (e) {
                    is IOException -> CurrencyApiThrowable.NetworkError
                    else -> CurrencyApiThrowable(e)
                }
            )
        }
    }

    override suspend fun getCurrencies(): Result<List<Currency>, GetCurrenciesThrowable> {
        return executeRequest { service.listCurrencies() }
            .map {
                it.symbols.values.map {
                    Currency(
                        shortName = it.code,
                        name = it.title
                    )
                }
            }
            .mapError {
                GetCurrenciesThrowable.General(it)
            }
    }

    override suspend fun getExchangeRate(
        from: Currency,
        to: Currency,
        amount: BigDecimal
    ): Result<Exchange, GetExchangeRateThrowable> {
        return executeRequest {
            service.getRates(
                from = from.shortName,
                to = to.shortName,
                amount = amount.toPlainString()
            )
        }
            .map { response ->
                response.rates.entries.map { entry ->
                    Exchange(
                        amount = amount,
                        rate = entry.value,
                        date = LocalDate.parse(response.date)
                    )
                }.first()
            }
            .mapError {
                GetExchangeRateThrowable.General(it)
            }
    }
}