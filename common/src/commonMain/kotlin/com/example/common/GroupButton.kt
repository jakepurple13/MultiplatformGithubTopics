package com.example.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class GroupButtonModel<T>(val item: T, val iconContent: @Composable () -> Unit)

@Composable
fun <T> GroupButton(
    selected: T,
    options: List<GroupButtonModel<T>>,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.surface,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: CornerBasedShape = RoundedCornerShape(20.0.dp),
    onClick: (T) -> Unit
) {
    Row(modifier) {
        val noCorner = CornerSize(0.dp)

        options.forEachIndexed { i, option ->
            OutlinedButton(
                modifier = Modifier,
                onClick = { onClick(option.item) },
                shape = shape.copy(
                    topStart = if (i == 0) shape.topStart else noCorner,
                    topEnd = if (i == options.size - 1) shape.topEnd else noCorner,
                    bottomStart = if (i == 0) shape.bottomStart else noCorner,
                    bottomEnd = if (i == options.size - 1) shape.bottomEnd else noCorner
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = animateColorAsState(if (selected == option.item) selectedColor else unselectedColor).value,
                    contentColor = animateColorAsState(if (selected == option.item) selectedContentColor else unselectedContentColor).value
                )
            ) { option.iconContent() }
        }
    }
}