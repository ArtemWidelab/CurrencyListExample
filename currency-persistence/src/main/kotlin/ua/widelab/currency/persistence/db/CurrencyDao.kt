package ua.widelab.currency.persistence.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.widelab.currency.persistence.models.entities.CurrencyEntity
import ua.widelab.currency.persistence.models.entities.CurrencyID
import ua.widelab.currency.persistence.models.entities.CurrencyPairEntity
import ua.widelab.currency.persistence.models.entities.CurrencyPairValue
import ua.widelab.currency.persistence.models.entities.ExchangeRateEntity

@Dao
internal interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(currency: List<CurrencyEntity>)

    @Query("DELETE FROM currency WHERE shortName NOT IN (:keepIds)")
    suspend fun deleteCurrencies(keepIds: List<CurrencyID>)

    @Query("SELECT * FROM currency")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(exchange: CurrencyPairEntity)

    @Query("DELETE FROM currency_pairs WHERE toCurrencyId = :toCurrencyID AND fromCurrencyId = :fromCurrencyID")
    suspend fun deleteCurrencyPair(toCurrencyID: CurrencyID, fromCurrencyID: CurrencyID)

    @Query("SELECT * FROM currency_pairs WHERE toCurrencyId = :toCurrencyID AND fromCurrencyId = :fromCurrencyID")
    suspend fun getCurrencyPair(
        toCurrencyID: CurrencyID,
        fromCurrencyID: CurrencyID
    ): CurrencyPairEntity?

    @MapInfo(
        keyColumn = "uid",
        keyTable = "currency_pairs",
        valueColumn = "uid",
        valueTable = "exchange_rate"
    )
    @Query(
        "SELECT *, MAX(exchange_rate.date) " +
                "FROM currency_pairs LEFT JOIN exchange_rate ON currency_pairs.uid = exchange_rate.currencyPairId " +
                "GROUP BY currency_pairs.uid"
    )
    fun getCurrencyPairsWithRates(): Flow<Map<CurrencyPairValue, ExchangeRateEntity?>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg rate: ExchangeRateEntity)


}