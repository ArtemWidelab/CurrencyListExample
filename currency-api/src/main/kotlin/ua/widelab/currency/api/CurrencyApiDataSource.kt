package ua.widelab.currency.api

import com.github.kittinunf.result.Result
import ua.widelab.app_network.findCause
import ua.widelab.currency.entities.models.Currency
import ua.widelab.currency.entities.models.Exchange
import java.math.BigDecimal

interface CurrencyApiDataSource {
    suspend fun getCurrencies(): Result<List<Currency>, GetCurrenciesThrowable>
    suspend fun getExchangeRate(
        from: Currency,
        to: Currency,
        amount: BigDecimal
    ): Result<Exchange, GetExchangeRateThrowable>
}

open class CurrencyApiThrowable(cause: Throwable) : Throwable(cause) {
    object NetworkError : CurrencyApiThrowable(Throwable("No Internet Connection"))

    fun isNetworkError(): Boolean {
        return findCause<NetworkError>(this) != null
    }
}

sealed class GetCurrenciesThrowable(cause: Throwable) : CurrencyApiThrowable(cause) {
    class General(cause: CurrencyApiThrowable) : GetCurrenciesThrowable(cause)
    //here can be some specific errors of the endpoint
}

sealed class GetExchangeRateThrowable(cause: Throwable) : CurrencyApiThrowable(cause) {
    class General(cause: CurrencyApiThrowable) : GetExchangeRateThrowable(cause)
    //here can be some specific errors of the endpoint
}
