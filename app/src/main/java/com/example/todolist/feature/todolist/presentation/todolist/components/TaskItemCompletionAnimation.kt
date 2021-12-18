package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.todolist.feature.todolist.presentation.util.noRippleClickable
import com.example.todolist.ui.theme.LightBlue


@Composable
fun TaskItemCompletionButton(
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Box(
       Modifier.noRippleClickable {
           onClick()
       }
    ) {
        Crossfade(targetState = isCompleted) { _isCompleted  ->
            if(_isCompleted){
                Icon(
                    Icons.Filled.Check, "completed taskItem",
                    tint = LightBlue
                )
            } else {
                Icon(
                    Icons.Outlined.Circle, "uncompleted taskItem",
                    tint = Color.LightGray
                )
            }
        }
    }
}