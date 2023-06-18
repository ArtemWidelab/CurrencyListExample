package ua.widelab.currency.persistence.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ua.widelab.currency.entities.models.Currency

@Entity(tableName = "currency")
internal data class CurrencyEntity(
    @PrimaryKey val shortName: CurrencyID,
    val name: String
) {

    companion object {
        fun fromCurrency(currency: Currency): CurrencyEntity {
            return CurrencyEntity(
                shortName = currency.shortName,
                name = currency.name
            )
        }

        fun CurrencyEntity.toCurrency(): Currency {
            return Currency(
                shortName = this.shortName,
                name = this.name
            )
        }
    }

}

typealias CurrencyID = String