package ua.widelab.currency.persistence.hilt

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ua.widelab.currency.persistence.CurrencyPersistenceDataSource
import ua.widelab.currency.persistence.RoomCurrencyPersistenceDataSource
import ua.widelab.currency.persistence.db.CurrencyDao
import ua.widelab.currency.persistence.db.CurrencyDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrencyPersistenceModule {

    companion object {
        @Singleton
        @Provides
        internal fun provideCurrencyDatabase(
            @ApplicationContext appContext: Context
        ): CurrencyDatabase {
            return Room.databaseBuilder(
                appContext,
                CurrencyDatabase::class.java,
                "currency-db"
            ).build()
        }

        @Singleton
        @Provides
        internal fun provideCurrencyDao(
            currencyDatabase: CurrencyDatabase
        ): CurrencyDao {
            return currencyDatabase.currencyDao()
        }
    }

    @Binds
    internal abstract fun bindCurrencyPersistenceDataSource(
        roomCurrencyPersistenceDataSource: RoomCurrencyPersistenceDataSource
    ): CurrencyPersistenceDataSource

}