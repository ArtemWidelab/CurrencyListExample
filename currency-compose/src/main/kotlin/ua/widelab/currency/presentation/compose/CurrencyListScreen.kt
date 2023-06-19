@file:OptIn(ExperimentalMaterial3Api::class)

package ua.widelab.currency.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.widelab.compose.components.SimpleAppBar
import ua.widelab.compose.components.SimpleAppBarNav
import ua.widelab.currency.compose.R
import ua.widelab.currency.presentation.CurrencyListViewModel
import ua.widelab.currency.presentation.CurrencyListViewModel.State.Companion.isBlockingLoading
import ua.widelab.currency.presentation.models.Currency
import ua.widelab.currency.presentation.models.ExchangeWithCurrency
import java.time.LocalDate

@Composable()
fun CurrencyListScreen(
    viewModel: CurrencyListViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsState()
    CurrencyListScreen(
        state,
        viewModel::addNewPair
    )
}

@Composable
fun CurrencyListScreen(
    state: CurrencyListViewModel.State,
    addNewPair: () -> Unit
) {
    if (state.isBlockingLoading) {
        CurrencyListLoading()
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
                ExchangeItem(exchangeWithCurrency = it)
            }
            state.newPairState?.let {
                item {
                    NewPairItem(
                        newPairState = it,
                        add = addNewPair
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun ExchangeItem(exchangeWithCurrency: ExchangeWithCurrency) {
    ItemCard(
        onClick = { /*TODO*/ }
    ) {
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
}

@Composable
fun NewPairItem(
    newPairState: CurrencyListViewModel.NewPairState,
    add: () -> Unit
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
                    currency = newPairState.from
                )
                Text(
                    style = MaterialTheme.typography.headlineSmall,
                    text = "-"
                )
                CurrencyChip(
                    modifier = Modifier.weight(1f),
                    currency = newPairState.to
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
fun CurrencyChip(modifier: Modifier, currency: Currency) {
    InputChip(
        modifier = modifier,
        selected = false,
        onClick = { /*TODO*/ },
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