package ua.widelab.currency.entities.models

import java.math.BigDecimal
import java.time.LocalDate

data class Exchange(
    val amount: BigDecimal,
    val rate: BigDecimal,
    val date: LocalDate
)