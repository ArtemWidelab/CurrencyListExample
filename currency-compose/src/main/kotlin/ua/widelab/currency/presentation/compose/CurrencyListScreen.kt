@file:OptIn(ExperimentalMaterial3Api::class)

package ua.widelab.currency.presentation.compose

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.widelab.compose.components.SimpleAppBar
import ua.widelab.compose.components.SimpleAppBarNav
import ua.widelab.currency.compose.R
import ua.widelab.currency.presentation.CurrencyListViewModel
import ua.widelab.currency.presentation.CurrencyListViewModel.State.Companion.isBlockingError
import ua.widelab.currency.presentation.CurrencyListViewModel.State.Companion.isBlockingLoading
import ua.widelab.currency.presentation.models.Currency
import ua.widelab.currency.presentation.models.ExchangeWithCurrency
import java.time.LocalDate

@Composable
fun CurrencyListScreen(
    viewModel: CurrencyListViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsState()
    CurrencyListScreen(
        state,
        viewModel::addNewPair,
        viewModel::delete,
        viewModel::setNewPairCurrency,
        viewModel::dismissCurrencySelection,
        viewModel::selectNewPairFrom,
        viewModel::selectNewPairTo,
        viewModel::load
    )

    val context = LocalContext.current

    LaunchedEffect(key1 = viewModel.actionsFlow) {
        viewModel.actionsFlow.collect {
            val message = when (it) {
                is CurrencyListViewModel.Action.ShowError -> it.throwable.message
                    ?: context.getString(R.string.unknown_error)

                CurrencyListViewModel.Action.ShowNoNetworkError -> context.getString(R.string.no_internet_connection)
                CurrencyListViewModel.Action.ShowAlreadyAddedError -> context.getString(R.string.such_pair_already_exists)
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun CurrencyListScreen(
    state: CurrencyListViewModel.State,
    addNewPair: () -> Unit,
    delete: (ExchangeWithCurrency) -> Unit,
    setNewPairValue: (Currency) -> Unit,
    dismissCurrencySelection: () -> Unit,
    selectNewPairFrom: () -> Unit,
    selectNewPairTo: () -> Unit,
    reload: () -> Unit
) {
    if (state.isBlockingLoading) {
        CurrencyListLoading()
        return
    }
    if (state.isBlockingError) {
        CurrencyListError(
            state = state,
            reload = reload
        )
        return
    }
    val lazyListState = rememberLazyListState()
    Scaffold(
        topBar = {
            SimpleAppBar(
                lazyListState = lazyListState,
                title = stringResource(id = R.string.currency_list_title),
                back = { },
                simpleAppBarNav = SimpleAppBarNav.Nothing
            )
        }
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = it
        ) {
            items(
                state.exchangeWithCurrencies,
                key = { it.toText() }
            ) {
                ExchangeItem(
                    exchangeWithCurrency = it,
                    delete = { delete(it) }
                )
            }
            state.newPairState?.let {
                item {
                    NewPairItem(
                        newPairState = it,
                        add = addNewPair,
                        selectNewPairFrom = selectNewPairFrom,
                        selectNewPairTo = selectNewPairTo
                    )
                }
            }
        }
    }
    val configuration = LocalConfiguration.current
    DropdownMenu(
        expanded = state.selectCurrencyState != CurrencyListViewModel.SelectCurrencyState.HIDDEN,
        onDismissRequest = dismissCurrencySelection,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        offset = DpOffset(
            x = configuration.screenWidthDp.times(0.1).dp,
            y = configuration.screenHeightDp.times(0.1).dp
        )
    ) {
        Box(
            modifier = Modifier.size(
                width = configuration.screenWidthDp.times(0.8).dp,
                height = configuration.screenHeightDp.times(0.8).dp
            )
        ) {
            LazyColumn {
                items(state.currencies) { currency ->
                    DropdownMenuItem(
                        text = {
                            Text(text = currency.name)
                        },
                        onClick = {
                            setNewPairValue(currency)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ExchangeItem(
    exchangeWithCurrency: ExchangeWithCurrency,
    delete: () -> Unit
) {
    ItemCard(
        onClick = { /*TODO*/ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    style = MaterialTheme.typography.headlineSmall,
                    text = exchangeWithCurrency.toText()
                )
                val date = exchangeWithCurrency.exchange?.date
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    text = when {
                        date?.isEqual(LocalDate.now()) == true -> stringResource(id = R.string.updated_today)
                        date == null && exchangeWithCurrency.loading -> stringResource(id = R.string.updating)
                        date == null -> stringResource(id = R.string.fetch_exchange_rate_error)
                        else -> stringResource(id = R.string.last_updated, date.toString())
                    }
                )
            }
            IconButton(
                onClick = delete,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun NewPairItem(
    newPairState: CurrencyListViewModel.NewPairState,
    add: () -> Unit,
    selectNewPairFrom: () -> Unit,
    selectNewPairTo: () -> Unit,
) {
    ItemCard(onClick = { /*TODO*/ }) {
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = stringResource(id = R.string.new_pair_block_title)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CurrencyChip(
                    modifier = Modifier.weight(1f),
                    currency = newPairState.from,
                    onClick = selectNewPairFrom
                )
                Text(
                    style = MaterialTheme.typography.headlineSmall,
                    text = "-"
                )
                CurrencyChip(
                    modifier = Modifier.weight(1f),
                    currency = newPairState.to,
                    onClick = selectNewPairTo
                )
            }
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = add
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
fun CurrencyChip(modifier: Modifier, currency: Currency, onClick: () -> Unit) {
    InputChip(
        modifier = modifier,
        selected = false,
        onClick = onClick,
        label = {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = currency.shortName,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    )
}

@Composable
fun CurrencyListLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
fun CurrencyListError(
    state: CurrencyListViewModel.State,
    reload: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(text = "Oops! " + state.loadingError?.message)
            Button(onClick = reload) {
                Text(text = stringResource(id = R.string.error_button_title))
            }
        }
    }
}