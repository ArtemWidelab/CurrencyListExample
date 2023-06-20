package ua.widelab.compose.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun DropdownLazyMenu(
    @FloatRange(0.0, 1.0) width: Float,
    @FloatRange(0.0, 1.0) height: Float,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val configuration = LocalConfiguration.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        offset = DpOffset(
            x = configuration.screenWidthDp.times((1 - width) / 2).dp,
            y = configuration.screenHeightDp.times((1 - height) / 2).dp
        )
    ) {
        Box(
            modifier = Modifier.size(
                width = configuration.screenWidthDp.times(width).dp,
                height = configuration.screenHeightDp.times(height).dp
            )
        ) {
            LazyColumn {
                content()
            }
        }
    }
}