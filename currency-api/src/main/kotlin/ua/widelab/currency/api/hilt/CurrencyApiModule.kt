package ua.widelab.currency.api.hilt

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ua.widelab.currency.api.CurrencyApiDataSource
import ua.widelab.currency.api.CurrencyApiDataSourceImpl
import ua.widelab.currency.api.CurrencyService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrencyApiModule {

    companion object {
        @Singleton
        @Provides
        internal fun provideCurrencyService(
            retrofit: Retrofit
        ): CurrencyService {
            return retrofit.create(CurrencyService::class.java)
        }
    }

    @Binds
    internal abstract fun bindCurrencyApiDataSource(
        currencyApiDataSourceImpl: CurrencyApiDataSourceImpl
    ): CurrencyApiDataSource

}