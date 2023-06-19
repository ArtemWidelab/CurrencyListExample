package ua.widelab.currency.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.widelab.currency.entities.models.CurrencyPair
import ua.widelab.currency.presentation.models.Currency
import ua.widelab.currency.presentation.models.Currency.Companion.toRepoModel
import ua.widelab.currency.presentation.models.Exchange
import ua.widelab.currency.presentation.models.ExchangeWithCurrency
import ua.widelab.currency.repo.CurrencyRepo
import javax.inject.Inject

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    private val repo: CurrencyRepo
) : ViewModel() {

    data class State(
        val currencies: ImmutableList<Currency> = persistentListOf(),
        val exchangeWithCurrencies: ImmutableList<ExchangeWithCurrency> = persistentListOf(),
        val loadingError: Throwable? = null,
        val newPairState: NewPairState? = null
    ) {
        companion object {
            val State.isBlockingLoading: Boolean
                get() = (this.loadingError == null && currencies.isEmpty() && exchangeWithCurrencies.isEmpty())
        }
    }

    data class NewPairState(
        val from: Currency,
        val to: Currency
    )

    private val mutableStateFlow by lazy { MutableStateFlow(State()) }
    val stateFlow: StateFlow<State> = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getCurrencyPairsWithRates()
                .collectLatest { list ->
                    mutableStateFlow.update {
                        it.copy(
                            exchangeWithCurrencies = list.mapNotNull {
                                val exchangeWithCurrency = it.data ?: return@mapNotNull null
                                ExchangeWithCurrency(
                                    exchange = exchangeWithCurrency.exchange?.let {
                                        Exchange.fromRepoModel(
                                            it
                                        )
                                    },
                                    toCurrency = Currency.fromRepoModel(exchangeWithCurrency.toCurrency),
                                    fromCurrency = Currency.fromRepoModel(exchangeWithCurrency.fromCurrency),
                                    loading = it.isLoading,
                                    error = it.error
                                )
                            }.toImmutableList()
                        )
                    }
                }
        }

        viewModelScope.launch {
            repo.addDefaultCurrencyPairs()
        }

        viewModelScope.launch {
            repo.getCurrencies()
                .collectLatest { endpointResult ->
                    mutableStateFlow.update {
                        it.copy(
                            currencies = endpointResult.data.orEmpty()
                                .map { Currency.fromRepoModel(it) }
                                .toImmutableList(),
                            loadingError = endpointResult.error
                        )
                    }
                }
        }

        viewModelScope.launch {
            mutableStateFlow
                .mapLatest { it.currencies }
                .filter { it.isNotEmpty() }
                .take(1)
                .collectLatest { list ->
                    mutableStateFlow.update {
                        it.copy(
                            newPairState = NewPairState(
                                from = list.random(),
                                to = list.random()
                            )
                        )
                    }
                }
        }
    }

    fun addNewPair() {
        val pairState = stateFlow.value.newPairState ?: return
        viewModelScope.launch {
            repo.addCurrencyPair(
                from = pairState.from.toRepoModel(),
                to = pairState.to.toRepoModel()
            )
        }
    }

    fun delete(exchangeWithCurrency: ExchangeWithCurrency) {
        viewModelScope.launch {
            repo.deleteCurrencyPair(
                CurrencyPair(
                    fromCurrency = exchangeWithCurrency.fromCurrency.toRepoModel(),
                    toCurrency = exchangeWithCurrency.toCurrency.toRepoModel()
                )
            )
        }
    }
}