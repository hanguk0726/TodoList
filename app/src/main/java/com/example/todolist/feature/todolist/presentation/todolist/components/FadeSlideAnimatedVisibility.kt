package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.compose.animation.*
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.unit.IntOffset

@Composable
fun FadeSlideAnimatedVisibility(
    visible: State<Boolean>,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn() + slideIn(
            animationSpec = TweenSpec(durationMillis = 200)
        ) { fullSize ->
            IntOffset(
                0,
                fullSize.height
            )
        },
        exit = fadeOut() + slideOut(
            animationSpec = TweenSpec(durationMillis = 200)
        ) { fullSize ->
            IntOffset(
                0,
                fullSize.height
            )
        },
    ) {
        content()
    }
}