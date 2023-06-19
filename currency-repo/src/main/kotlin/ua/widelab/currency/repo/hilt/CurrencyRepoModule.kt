package ua.widelab.currency.repo.hilt

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ua.widelab.currency.repo.CurrencyRepo
import ua.widelab.currency.repo.CurrencyRepoImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CurrencyRepoModule {
    companion object {

        @Provides
        @Singleton
        internal fun provideDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                produceFile = {
                    context.preferencesDataStoreFile("currency")
                }
            )
        }
    }

    @Binds
    @Singleton
    internal abstract fun bindCurrencyRepo(
        currencyRepoImpl: CurrencyRepoImpl
    ): CurrencyRepo
}