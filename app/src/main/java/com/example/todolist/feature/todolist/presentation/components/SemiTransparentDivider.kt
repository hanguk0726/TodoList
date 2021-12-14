package com.example.todolist.feature.todolist.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SemiTransparentDivider() {
    Divider(
        color = Color.Gray.copy(0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    )
}