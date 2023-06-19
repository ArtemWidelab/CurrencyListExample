package ua.widelab.currency.presentation.models

import java.math.BigDecimal
import java.time.LocalDate

data class Exchange(
    val amount: BigDecimal,
    val rate: BigDecimal,
    val date: LocalDate
) {
    companion object {
        fun fromRepoModel(exchange: ua.widelab.currency.entities.models.Exchange): Exchange {
            return Exchange(
                amount = exchange.amount,
                rate = exchange.rate,
                date = exchange.date
            )
        }
    }
}