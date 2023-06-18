package ua.widelab.currency.persistence.models.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import ua.widelab.currency.entities.models.CurrencyPair

@Entity(
    tableName = "currency_pairs",
    indices = [
        Index(
            value = ["fromCurrencyId", "toCurrencyId"],
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = CurrencyEntity::class,
            parentColumns = ["shortName"],
            childColumns = ["fromCurrencyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CurrencyEntity::class,
            parentColumns = ["shortName"],
            childColumns = ["toCurrencyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class CurrencyPairEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val fromCurrencyId: String,
    val toCurrencyId: String
) {
    companion object {
        fun fromCurrencyPair(currencyPair: CurrencyPair): CurrencyPairEntity {
            return CurrencyPairEntity(
                fromCurrencyId = currencyPair.fromCurrency.shortName,
                toCurrencyId = currencyPair.toCurrency.shortName
            )
        }
    }
}

internal data class CurrencyPairValue(
    @Embedded val currencyPairEntity: CurrencyPairEntity,
    @Relation(
        parentColumn = "fromCurrencyId",
        entityColumn = "shortName"
    )
    val fromCurrency: CurrencyEntity,
    @Relation(
        parentColumn = "toCurrencyId",
        entityColumn = "shortName"
    )
    val toCurrency: CurrencyEntity,
)