package com.example.todolist.feature.todolist.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.theme.Blue
import com.example.todolist.ui.theme.DarkWhite

// Theme color applying needed
@Composable
fun CustomSnackbarHost(
    state: SnackbarHostState
) {
    SnackbarHost(state) { data ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            backgroundColor = DarkWhite,
            content = {
                Text(
                    text = data.message,
                    color = Color.Black,
                    style = MaterialTheme.typography.body1
                )
            },
            action = {
                data.actionLabel?.let { actionLabel ->
                    TextButton(onClick = { data.performAction() }) {
                        Text(
                            text = actionLabel,
                            color = Blue,
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            })
    }
}