package ua.widelab.currency.persistence.models.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ua.widelab.currency.entities.models.Exchange
import java.math.BigDecimal
import java.time.LocalDate

@Entity(
    tableName = "exchange_rate",
    indices = [
        Index(
            value = ["currencyPairId", "date", "amount"],
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = CurrencyPairEntity::class,
            parentColumns = ["uid"],
            childColumns = ["currencyPairId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val currencyPairId: Long,
    val amount: BigDecimal,
    val rate: BigDecimal,
    val date: LocalDate
) {
    companion object {

        fun ExchangeRateEntity.toExchange(): Exchange {
            return Exchange(
                amount = this.amount,
                rate = this.rate,
                date = this.date
            )
        }

        fun fromExchange(exchange: Exchange, currencyPairId: Long): ExchangeRateEntity {
            return ExchangeRateEntity(
                currencyPairId = currencyPairId,
                amount = exchange.amount,
                rate = exchange.rate,
                date = exchange.date
            )
        }
    }
}