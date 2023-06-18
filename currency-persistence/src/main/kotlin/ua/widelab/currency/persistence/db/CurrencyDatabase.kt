package ua.widelab.currency.persistence.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ua.widelab.currency.persistence.models.entities.CurrencyEntity
import ua.widelab.currency.persistence.models.entities.CurrencyPairEntity
import ua.widelab.currency.persistence.models.entities.ExchangeRateEntity

@Database(
    entities = [CurrencyEntity::class, CurrencyPairEntity::class, ExchangeRateEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
internal abstract class CurrencyDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
}