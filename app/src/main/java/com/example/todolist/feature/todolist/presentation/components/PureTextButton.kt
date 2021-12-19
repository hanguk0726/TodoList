package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PureTextButton(
    text: String,
    textColor: Color,
    paddingValues: PaddingValues = PaddingValues(8.dp),
    onClick: () -> Unit
) {
    Box(
        Modifier.clickable(
            onClick = onClick
        ).padding(paddingValues)
    ) {
        Text(text, color = textColor)
    }
}