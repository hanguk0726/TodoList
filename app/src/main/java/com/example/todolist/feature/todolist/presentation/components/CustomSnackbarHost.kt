package com.example.todolist.feature.todolist.presentation.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.common.ui.theme.Blue
import com.example.todolist.common.ui.theme.LightBlue

@Composable
fun CustomSnackbarHost(
    state: SnackbarHostState
) {
    SnackbarHost(state) { data ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            backgroundColor = if(isSystemInDarkTheme()) lightColors().background else
                darkColors().background,
            content = {
                Text(
                    text = data.message,
                    color =   if(isSystemInDarkTheme()) lightColors().onBackground else
                        darkColors().onBackground,
                    style = MaterialTheme.typography.body1
                )
            },
            action = {
                data.actionLabel?.let { actionLabel ->
                    TextButton(onClick = { data.performAction() }) {
                        Text(
                            text = actionLabel,
                            color = if(isSystemInDarkTheme()) Blue else
                                LightBlue,
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            })
    }
}