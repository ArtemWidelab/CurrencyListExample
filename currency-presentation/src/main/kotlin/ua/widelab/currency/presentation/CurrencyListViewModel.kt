package ua.widelab.currency.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.widelab.currency.entities.models.CurrencyPair
import ua.widelab.currency.presentation.CurrencyListViewModel.State.Companion.isBlockingError
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

    private val mutableStateFlow by lazy { MutableStateFlow(State()) }
    val stateFlow: StateFlow<State> = mutableStateFlow.asStateFlow()

    private val actionsChannel = Channel<Action>()
    val actionsFlow: Flow<Action> = actionsChannel.receiveAsFlow()

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

        load()

        viewModelScope.launch {
            repo.addDefaultCurrencyPairs()
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

        viewModelScope.launch {
            mutableStateFlow
                .filter { it.loadingError != null && !it.isBlockingError }
                .mapNotNull { it.loadingError }
                .collectLatest {
                    actionsChannel.send(Action.ShowError(it))
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
                .onFailure {
                    actionsChannel.send(Action.ShowAlreadyAddedError)
                }
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

    fun setNewPairCurrency(currency: Currency) {
        mutableStateFlow.update {
            when (it.selectCurrencyState) {
                SelectCurrencyState.HIDDEN -> it
                SelectCurrencyState.FROM -> it.copy(
                    newPairState = it.newPairState?.copy(
                        from = currency
                    ),
                    selectCurrencyState = SelectCurrencyState.HIDDEN
                )

                SelectCurrencyState.TO -> it.copy(
                    newPairState = it.newPairState?.copy(
                        to = currency
                    ),
                    selectCurrencyState = SelectCurrencyState.HIDDEN
                )
            }

        }
    }

    fun dismissCurrencySelection() {
        mutableStateFlow.update {
            it.copy(
                selectCurrencyState = SelectCurrencyState.HIDDEN
            )
        }
    }

    fun selectNewPairFrom() {
        mutableStateFlow.update {
            it.copy(
                selectCurrencyState = SelectCurrencyState.FROM
            )
        }
    }

    fun selectNewPairTo() {
        mutableStateFlow.update {
            it.copy(
                selectCurrencyState = SelectCurrencyState.TO
            )
        }
    }

    private var loadCurrenciesJob: Job? = null
    fun load() {
        loadCurrenciesJob?.cancel()
        loadCurrenciesJob = viewModelScope.launch {
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
    }

    @Stable
    data class State(
        val currencies: ImmutableList<Currency> = persistentListOf(),
        val exchangeWithCurrencies: ImmutableList<ExchangeWithCurrency> = persistentListOf(),
        val loadingError: Throwable? = null,
        val newPairState: NewPairState? = null,
        val selectCurrencyState: SelectCurrencyState = SelectCurrencyState.HIDDEN
    ) {
        companion object {
            val State.isBlockingLoading: Boolean
                get() = (this.loadingError == null && currencies.isEmpty() && exchangeWithCurrencies.isEmpty())

            val State.isBlockingError: Boolean
                get() = (this.loadingError != null && currencies.isEmpty() && exchangeWithCurrencies.isEmpty())
        }
    }

    @Stable
    data class NewPairState(
        val from: Currency,
        val to: Currency
    )

    @Stable
    enum class SelectCurrencyState {
        HIDDEN,
        FROM,
        TO
    }

    sealed interface Action {
        class ShowError(val throwable: Throwable) : Action
        object ShowNoNetworkError : Action
        object ShowAlreadyAddedError : Action
        //openCurrencyPair
    }
}