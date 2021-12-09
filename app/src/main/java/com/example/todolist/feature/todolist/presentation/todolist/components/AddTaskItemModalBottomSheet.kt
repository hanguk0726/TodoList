package com.example.todolist.feature.todolist.presentation.todolist.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.theme.scrimColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun AddTaskItemModalBottomSheet(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    focusRequester: FocusRequester,
    textField: @Composable () -> Unit,
){

    val isShowing = state.targetValue != ModalBottomSheetValue.Hidden

    val keyboardController = LocalSoftwareKeyboardController.current

    if(isShowing){
        DisposableEffect(Unit) {
            focusRequester.requestFocus()
            onDispose { }
        }
    } else {
        keyboardController?.hide()
        LocalFocusManager.current.clearFocus()
    }

    BackHandler(
        enabled = isShowing,
        onBack = {
            scope.launch {
                keyboardController?.hide()
                state.animateTo(ModalBottomSheetValue.Hidden, tween(300))
            }
        }
    )

    ModalBottomSheetLayout(
        sheetState = state,
        scrimColor = scrimColor,
        sheetContent = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                textField()
            }
        },
    ) {

    }
}
