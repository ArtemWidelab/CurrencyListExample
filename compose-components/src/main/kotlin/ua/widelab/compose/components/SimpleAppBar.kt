@file:OptIn(ExperimentalMaterial3Api::class)

package ua.widelab.compose.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class SimpleAppBarNav {
    Close,
    Back,
    Nothing
}

@Composable
fun SimpleAppBar(
    lazyListState: LazyListState,
    title: String,
    back: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    simpleAppBarNav: SimpleAppBarNav,
) {
    val elevation by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemScrollOffset == 0) 0.dp else 4.dp
        }
    }
    val elevationAnim by animateDpAsState(targetValue = elevation)
    TopAppBar(
        title = {
            Text(
                modifier = Modifier,
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = Modifier.shadow(elevationAnim),
        navigationIcon = {
            when (simpleAppBarNav) {
                //                SimpleAppBarNav.Close -> CloseIcon(back)
                //                SimpleAppBarNav.Back -> BackIcon(back)
                SimpleAppBarNav.Nothing -> {}
                else -> TODO("not implemented")
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}