package ua.widelab.currency.presentation.models

data class Currency(
    val shortName: String,
    val name: String
) {
    companion object {
        fun fromRepoModel(currency: ua.widelab.currency.entities.models.Currency): Currency {
            return Currency(
                shortName = currency.shortName,
                name = currency.name
            )
        }

        fun Currency.toRepoModel(): ua.widelab.currency.entities.models.Currency {
            return ua.widelab.currency.entities.models.Currency(
                shortName = this.shortName,
                name = this.name
            )
        }
    }
}