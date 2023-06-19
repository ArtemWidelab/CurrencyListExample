package ua.widelab.currency.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CurrenciesResponse(
    val success: Boolean,
    val symbols: Map<String, SymbolData>
)

@Serializable
internal data class SymbolData(
    @SerialName("description") val title: String,
    val code: String
)