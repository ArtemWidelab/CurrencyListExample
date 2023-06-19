package ua.widelab.currencylistexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import ua.widelab.currency.presentation.compose.CurrencyListScreen
import ua.widelab.currencylistexample.ui.theme.CurrencyListExampleTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyListExampleTheme {
                CurrencyListScreen()
            }
        }
    }
}