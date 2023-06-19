package ua.widelab.currency.api.models

import kotlinx.serialization.Serializable
import ua.widelab.app_network.serializers.BigDecimalNumericSerializer
import java.math.BigDecimal

@Serializable
internal data class RatesResponse(
    val success: Boolean,
    val base: String,
    val date: String,
    val rates: Map<String, @Serializable(with = BigDecimalNumericSerializer::class) BigDecimal>
)

