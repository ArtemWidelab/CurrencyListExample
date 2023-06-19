package ua.widelab.currency.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ua.widelab.currency.api.models.CurrenciesResponse
import ua.widelab.currency.api.models.RatesResponse

internal interface CurrencyService {
    @GET("symbols")
    suspend fun listCurrencies(): Response<CurrenciesResponse>

    @GET("latest")
    suspend fun getRates(
        @Query("base") from: String,
        @Query("amount") amount: String,
        @Query("symbols") to: String
    ): Response<RatesResponse>
}